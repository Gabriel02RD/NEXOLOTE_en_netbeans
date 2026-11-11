package com.nexolote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Consulta de trazabilidad.
 *  - Pestaña 1: dado un lote producido, muestra su origen (materias primas y proveedores),
 *    los equipos usados, su recorrido por las ubicaciones y las incidencias.
 *  - Pestaña 2: rastreo inverso. Dado un lote de materia prima, muestra en qué lotes
 *    producidos se usó y dónde están ahora (útil para un retiro del mercado).
 */
public class VentanaTrazabilidad extends JFrame {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Ubicación actual de un lote producido = destino de su último movimiento. */
    private static final String UBICACION_ACTUAL =
            "CASE WHEN m.id_movimiento IS NULL THEN 'Sin movimientos registrados' "
            + "WHEN m.tipo_movimiento = 'Salida' THEN 'Fuera del establecimiento' ELSE u.nombre END";
    private static final String JOIN_ULTIMO_MOVIMIENTO =
            "LEFT JOIN MOVIMIENTO_LOTE m ON m.id_movimiento = (SELECT m2.id_movimiento FROM MOVIMIENTO_LOTE m2 "
            + "WHERE m2.id_lote = lp.id_lote ORDER BY m2.fecha_movimiento DESC, m2.id_movimiento DESC LIMIT 1) "
            + "LEFT JOIN UBICACION u ON u.id_ubicacion = m.id_ubicacion_destino ";

    private final JComboBox<Estilos.Opcion> cbLote = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbLoteMp = new JComboBox<>();

    private final JLabel lblProducto = valor(), lblResponsable = valor(), lblProduccion = valor(),
            lblVencimiento = valor(), lblCantidad = valor(), lblEstado = valor(), lblUbicacion = valor();
    private final JLabel lblInfoMp = new JLabel(" ");

    private final DefaultTableModel mMaterias = modelo("Lote MP", "Materia prima", "Proveedor", "Recepción", "Vencimiento", "Cant. usada", "Unidad");
    private final DefaultTableModel mEquipos = modelo("Código", "Equipo", "Tipo", "Observaciones");
    private final DefaultTableModel mMovimientos = modelo("Fecha", "Tipo", "Origen", "Destino", "Observación");
    private final DefaultTableModel mIncidencias = modelo("Fecha", "Tipo", "Descripción", "Estado", "Reportó");
    private final DefaultTableModel mInverso = modelo("Lote producido", "Producto", "Cant. usada", "Estado del lote", "Ubicación actual");

    public VentanaTrazabilidad() {
        setTitle("NEXOLOTE | Trazabilidad");
        setSize(1180, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Trazabilidad de lotes",
                "Consulta el origen, los equipos, el recorrido y las incidencias de cada lote."), BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(Estilos.NEGRITA);
        pestanas.addTab("Origen y recorrido de un lote", crearPestanaLote());
        pestanas.addTab("Rastreo inverso de materia prima", crearPestanaInversa());
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Estilos.FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(14, 22, 18, 22));
        centro.add(pestanas, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        cargarListas();
    }

    // ------------------------------------------------------------------ pestañas

    private JPanel crearPestanaLote() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Estilos.FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(14, 4, 4, 4));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barra.setOpaque(false);
        barra.add(Estilos.crearEtiqueta("Lote de producción"));
        cbLote.setPreferredSize(new java.awt.Dimension(380, Estilos.ALTO_CAMPO));
        Estilos.estilizarCombo(cbLote);
        barra.add(cbLote);
        JButton bConsultar = Estilos.botonGuardar("Consultar");
        JButton bActualizar = Estilos.botonLimpiar("Actualizar listas");
        bConsultar.addActionListener(e -> Estilos.ejecutar(this, this::consultarLote));
        bActualizar.addActionListener(e -> cargarListas());
        barra.add(bConsultar);
        barra.add(bActualizar);

        JPanel resumen = Estilos.crearPanelTarjeta();
        JPanel datos = new JPanel(new GridLayout(2, 4, 16, 6));
        datos.setOpaque(false);
        agregarDato(datos, "Producto", lblProducto);
        agregarDato(datos, "Responsable", lblResponsable);
        agregarDato(datos, "Producción", lblProduccion);
        agregarDato(datos, "Vencimiento", lblVencimiento);
        agregarDato(datos, "Cantidad", lblCantidad);
        agregarDato(datos, "Estado", lblEstado);
        agregarDato(datos, "Ubicación actual", lblUbicacion);
        datos.add(new JLabel());
        resumen.add(datos, BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.setOpaque(false);
        norte.add(barra, BorderLayout.NORTH);
        norte.add(resumen, BorderLayout.CENTER);
        p.add(norte, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(2, 2, 12, 12));
        rejilla.setOpaque(false);
        rejilla.add(panelTabla("Origen: materias primas y proveedores", mMaterias));
        rejilla.add(panelTabla("Equipos utilizados", mEquipos));
        rejilla.add(panelTabla("Recorrido: movimientos entre ubicaciones", mMovimientos));
        rejilla.add(panelTabla("Incidencias reportadas", mIncidencias));
        p.add(rejilla, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPestanaInversa() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Estilos.FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(14, 4, 4, 4));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barra.setOpaque(false);
        barra.add(Estilos.crearEtiqueta("Lote de materia prima"));
        cbLoteMp.setPreferredSize(new java.awt.Dimension(380, Estilos.ALTO_CAMPO));
        Estilos.estilizarCombo(cbLoteMp);
        barra.add(cbLoteMp);
        JButton bConsultar = Estilos.botonGuardar("Consultar");
        bConsultar.addActionListener(e -> Estilos.ejecutar(this, this::consultarInverso));
        barra.add(bConsultar);

        lblInfoMp.setFont(Estilos.SUBTITULO);
        lblInfoMp.setForeground(Estilos.TEXTO_SEC);
        JPanel norte = new JPanel(new BorderLayout(0, 8));
        norte.setOpaque(false);
        norte.add(barra, BorderLayout.NORTH);
        norte.add(lblInfoMp, BorderLayout.CENTER);
        p.add(norte, BorderLayout.NORTH);
        p.add(panelTabla("Lotes producidos donde se utilizó este lote de materia prima", mInverso), BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------------ consultas

    private void consultarLote() {
        if (cbLote.getItemCount() == 0) throw new Estilos.ValidacionException("Todavía no hay lotes de producción registrados.");
        int id = Estilos.idDe(cbLote);

        try (Connection con = Conexion.conectar()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT p.nombre, CONCAT(r.nombres,' ',r.apellidos), lp.fecha_produccion, lp.fecha_vencimiento, "
                    + "CONCAT(lp.cantidad_producida,' ',p.unidad_medida), lp.estado, " + UBICACION_ACTUAL + " "
                    + "FROM LOTE_PRODUCCION lp "
                    + "INNER JOIN PRODUCTO p ON p.id_producto = lp.id_producto "
                    + "INNER JOIN RESPONSABLE r ON r.id_responsable = lp.id_responsable "
                    + JOIN_ULTIMO_MOVIMIENTO + "WHERE lp.id_lote = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        lblProducto.setText(texto(rs.getObject(1)));
                        lblResponsable.setText(texto(rs.getObject(2)));
                        lblProduccion.setText(texto(rs.getObject(3)));
                        lblVencimiento.setText(texto(rs.getObject(4)));
                        lblCantidad.setText(texto(rs.getObject(5)));
                        lblEstado.setText(texto(rs.getObject(6)));
                        lblUbicacion.setText(texto(rs.getObject(7)));
                    }
                }
            }
        } catch (SQLException e) {
            Conexion.mostrarError(this, e);
            return;
        }

        llenar(mMaterias,
                "SELECT lmp.codigo_lote_mp, mp.nombre, prov.nombre, lmp.fecha_recepcion, lmp.fecha_vencimiento, "
                + "c.cantidad_utilizada, mp.unidad_medida "
                + "FROM CONSUMO_MATERIA_PRIMA c "
                + "INNER JOIN LOTE_MATERIA_PRIMA lmp ON lmp.id_lote_mp = c.id_lote_mp "
                + "INNER JOIN MATERIA_PRIMA mp ON mp.id_materia_prima = lmp.id_materia_prima "
                + "INNER JOIN PROVEEDOR prov ON prov.id_proveedor = lmp.id_proveedor "
                + "WHERE c.id_lote = ? ORDER BY mp.nombre, lmp.codigo_lote_mp", id);
        llenar(mEquipos,
                "SELECT e.codigo_inventario, e.nombre, e.tipo_equipo, u.observaciones "
                + "FROM USO_EQUIPO_LOTE u INNER JOIN EQUIPO e ON e.id_equipo = u.id_equipo "
                + "WHERE u.id_lote = ? ORDER BY e.codigo_inventario", id);
        llenar(mMovimientos,
                "SELECT m.fecha_movimiento, m.tipo_movimiento, o.nombre, d.nombre, m.observacion "
                + "FROM MOVIMIENTO_LOTE m "
                + "LEFT JOIN UBICACION o ON o.id_ubicacion = m.id_ubicacion_origen "
                + "LEFT JOIN UBICACION d ON d.id_ubicacion = m.id_ubicacion_destino "
                + "WHERE m.id_lote = ? ORDER BY m.fecha_movimiento, m.id_movimiento", id);
        llenar(mIncidencias,
                "SELECT i.fecha_registro, i.tipo_incidencia, i.descripcion, i.estado, CONCAT(r.nombres,' ',r.apellidos) "
                + "FROM INCIDENCIA i LEFT JOIN RESPONSABLE r ON r.id_responsable = i.id_responsable "
                + "WHERE i.id_lote = ? ORDER BY i.fecha_registro", id);
    }

    private void consultarInverso() {
        if (cbLoteMp.getItemCount() == 0) throw new Estilos.ValidacionException("Todavía no hay lotes de materia prima registrados.");
        int id = Estilos.idDe(cbLoteMp);

        try (Connection con = Conexion.conectar()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT CONCAT(mp.nombre,' · proveedor: ',prov.nombre,' · recibido el ',lmp.fecha_recepcion,"
                    + "' · cantidad recibida: ',lmp.cantidad_recibida,' ',mp.unidad_medida) "
                    + "FROM LOTE_MATERIA_PRIMA lmp "
                    + "INNER JOIN MATERIA_PRIMA mp ON mp.id_materia_prima = lmp.id_materia_prima "
                    + "INNER JOIN PROVEEDOR prov ON prov.id_proveedor = lmp.id_proveedor WHERE lmp.id_lote_mp = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    lblInfoMp.setText(rs.next() ? rs.getString(1) : " ");
                }
            }
        } catch (SQLException e) {
            Conexion.mostrarError(this, e);
            return;
        }

        llenar(mInverso,
                "SELECT lp.codigo_lote, p.nombre, c.cantidad_utilizada, lp.estado, " + UBICACION_ACTUAL + " "
                + "FROM CONSUMO_MATERIA_PRIMA c "
                + "INNER JOIN LOTE_PRODUCCION lp ON lp.id_lote = c.id_lote "
                + "INNER JOIN PRODUCTO p ON p.id_producto = lp.id_producto "
                + JOIN_ULTIMO_MOVIMIENTO
                + "WHERE c.id_lote_mp = ? ORDER BY lp.fecha_produccion, lp.codigo_lote", id);
        if (mInverso.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Este lote de materia prima todavía no se ha usado en ningún lote producido.",
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ------------------------------------------------------------------ utilidades

    private void cargarListas() {
        cargarCombo(cbLote,
                "SELECT lp.id_lote, CONCAT(lp.codigo_lote,' · ',p.nombre) FROM LOTE_PRODUCCION lp "
                + "INNER JOIN PRODUCTO p ON p.id_producto = lp.id_producto ORDER BY lp.fecha_produccion DESC, lp.id_lote DESC");
        cargarCombo(cbLoteMp,
                "SELECT lmp.id_lote_mp, CONCAT(lmp.codigo_lote_mp,' · ',mp.nombre) FROM LOTE_MATERIA_PRIMA lmp "
                + "INNER JOIN MATERIA_PRIMA mp ON mp.id_materia_prima = lmp.id_materia_prima "
                + "ORDER BY lmp.fecha_recepcion DESC, lmp.id_lote_mp DESC");
    }

    private void cargarCombo(JComboBox<Estilos.Opcion> combo, String sql) {
        combo.removeAllItems();
        try (Connection con = Conexion.conectar()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) combo.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            Conexion.mostrarError(this, e);
        }
    }

    private void llenar(DefaultTableModel modelo, String sql, int id) {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    int columnas = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        Object[] fila = new Object[columnas];
                        for (int i = 0; i < columnas; i++) fila[i] = texto(rs.getObject(i + 1));
                        modelo.addRow(fila);
                    }
                }
            }
        } catch (SQLException e) {
            Conexion.mostrarError(this, e);
        }
    }

    private static String texto(Object v) {
        if (v == null) return "—";
        if (v instanceof LocalDateTime t) return t.format(FECHA_HORA);
        if (v instanceof java.sql.Timestamp t) return t.toLocalDateTime().format(FECHA_HORA);
        if (v instanceof java.math.BigDecimal b) return b.stripTrailingZeros().toPlainString();
        return v.toString();
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    private JPanel panelTabla(String titulo, DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        Estilos.estilizarTabla(tabla);
        tabla.setRowHeight(26);
        JPanel panel = Estilos.crearPanelTabla(titulo);
        panel.add(Estilos.crearScroll(tabla), BorderLayout.CENTER);
        return panel;
    }

    private static JLabel valor() {
        JLabel l = new JLabel("—");
        l.setFont(Estilos.NORMAL);
        l.setForeground(Estilos.TEXTO);
        return l;
    }

    private static void agregarDato(JPanel panel, String etiqueta, JLabel valor) {
        JPanel celda = new JPanel(new BorderLayout(0, 2));
        celda.setOpaque(false);
        JLabel l = new JLabel(etiqueta.toUpperCase());
        l.setFont(Estilos.KPI_LABEL);
        l.setForeground(Estilos.TEXTO_SEC);
        celda.add(l, BorderLayout.NORTH);
        celda.add(valor, BorderLayout.CENTER);
        panel.add(celda);
    }
}
