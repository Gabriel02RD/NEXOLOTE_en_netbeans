package com.nexolote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class VentanaMovimientosLote extends JFrame {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JComboBox<Estilos.Opcion> cbLote = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbOrigen = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbDestino = new JComboBox<>();
    private final JTextField txtFecha = new JTextField();
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Ingreso", "Traslado", "Salida"});
    private final JTextArea txtObs = new JTextArea(3, 20);
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Lote", "Origen", "Destino", "Fecha", "Tipo", "Observación"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = -1;

    public VentanaMovimientosLote() {
        setTitle("NEXOLOTE | Movimientos de lotes"); setSize(1240, 760); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Movimientos de lotes", "Traslados y cambios de ubicación."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Datos del movimiento");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Lote de producción *", cbLote); Estilos.agregarCampo(form, gbc, 2, 0, "Ubicación origen", cbOrigen);
        Estilos.agregarCampo(form, gbc, 0, 1, "Ubicación destino", cbDestino); Estilos.agregarCampo(form, gbc, 2, 1, "Fecha * (AAAA-MM-DD HH:MM:SS)", txtFecha);
        Estilos.agregarCampo(form, gbc, 0, 2, "Tipo *", cbTipo);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.insets = new java.awt.Insets(5, 5, 5, 8);
        form.add(Estilos.crearEtiqueta("Observación"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 1; gbc.insets = new java.awt.Insets(5, 5, 5, 14);
        Estilos.estilizarAreaTexto(txtObs); form.add(new JScrollPane(txtObs), gbc);
        cardForm.add(form, BorderLayout.CENTER); superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Registrar"), bActualizar = Estilos.botonActualizar("Modificar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Historial de movimientos");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargarCombos(); cargar();
        txtFecha.setText(LocalDateTime.now().format(FORMATO));
    }

    private void cargarCombos() { cargarOp(cbLote, "SELECT id_lote, codigo_lote FROM LOTE_PRODUCCION ORDER BY codigo_lote"); cargarUbi(cbOrigen); cargarUbi(cbDestino); }
    private void cargarOp(JComboBox<Estilos.Opcion> cb, String sql) { cb.removeAllItems(); try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) cb.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); } }
    private void cargarUbi(JComboBox<Estilos.Opcion> cb) { cb.removeAllItems(); cb.addItem(new Estilos.Opcion(-1, "Sin asignar")); try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT id_ubicacion, nombre FROM UBICACION ORDER BY nombre"); ResultSet rs = ps.executeQuery()) { while (rs.next()) cb.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT m.id_movimiento, l.codigo_lote, o.nombre, d.nombre, m.fecha_movimiento, m.tipo_movimiento, m.observacion FROM MOVIMIENTO_LOTE m INNER JOIN LOTE_PRODUCCION l ON m.id_lote=l.id_lote LEFT JOIN UBICACION o ON m.id_ubicacion_origen=o.id_ubicacion LEFT JOIN UBICACION d ON m.id_ubicacion_destino=d.id_ubicacion ORDER BY m.fecha_movimiento DESC"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3) == null ? "Sin asignar" : rs.getString(3), rs.getString(4) == null ? "Sin asignar" : rs.getString(4), rs.getTimestamp(5), rs.getString(6), rs.getString(7)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() { if (cbLote.getSelectedItem() == null) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO MOVIMIENTO_LOTE (id_lote, id_ubicacion_origen, id_ubicacion_destino, fecha_movimiento, tipo_movimiento, observacion) VALUES (?,?,?,?,?,?)")) { llenar(ps); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }
    private void actualizar() { if (idSeleccionado == -1) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE MOVIMIENTO_LOTE SET id_lote=?, id_ubicacion_origen=?, id_ubicacion_destino=?, fecha_movimiento=?, tipo_movimiento=?, observacion=? WHERE id_movimiento=?")) { llenar(ps); ps.setInt(7, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void llenar(PreparedStatement ps) throws SQLException {
        Estilos.Opcion vo = (Estilos.Opcion) cbOrigen.getSelectedItem(), vd = (Estilos.Opcion) cbDestino.getSelectedItem();
        Estilos.validarMovimiento(cbTipo.getSelectedItem().toString(), vo == null ? -1 : vo.id, vd == null ? -1 : vd.id);
        ps.setInt(1, Estilos.idDe(cbLote));
        Estilos.Opcion o = (Estilos.Opcion) cbOrigen.getSelectedItem(); if (o == null || o.id == -1) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, o.id);
        Estilos.Opcion d = (Estilos.Opcion) cbDestino.getSelectedItem(); if (d == null || d.id == -1) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, d.id);
        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.parse(txtFecha.getText().trim(), FORMATO))); ps.setString(5, cbTipo.getSelectedItem().toString()); ps.setString(6, txtObs.getText().trim());
    }

    private void eliminar() { if (idSeleccionado == -1) return; if (JOptionPane.showConfirmDialog(this, "¿Eliminar el movimiento?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM MOVIMIENTO_LOTE WHERE id_movimiento=?")) { ps.setInt(1, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow()); idSeleccionado = Integer.parseInt(modelo.getValueAt(f, 0).toString());
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT * FROM MOVIMIENTO_LOTE WHERE id_movimiento=?")) { ps.setInt(1, idSeleccionado); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { seleccionarOp(cbLote, rs.getInt("id_lote")); int o = rs.getInt("id_ubicacion_origen"); seleccionarOp(cbOrigen, rs.wasNull() ? -1 : o); int d = rs.getInt("id_ubicacion_destino"); seleccionarOp(cbDestino, rs.wasNull() ? -1 : d); Timestamp t = rs.getTimestamp("fecha_movimiento"); txtFecha.setText(t == null ? "" : t.toLocalDateTime().format(FORMATO)); cbTipo.setSelectedItem(rs.getString("tipo_movimiento")); txtObs.setText(rs.getString("observacion") == null ? "" : rs.getString("observacion")); } } } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }
    private void seleccionarOp(JComboBox<Estilos.Opcion> cb, int id) { for (int i = 0; i < cb.getItemCount(); i++) if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; } }
    private void limpiar() { idSeleccionado = -1; txtFecha.setText(LocalDateTime.now().format(FORMATO)); txtObs.setText(""); tabla.clearSelection(); }
}