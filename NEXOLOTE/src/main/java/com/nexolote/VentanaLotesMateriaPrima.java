package com.nexolote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class VentanaLotesMateriaPrima extends JFrame {
    private final JTextField txtCodigo = new JTextField();
    private final JComboBox<Estilos.Opcion> cbMateria = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbProv = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbUbi = new JComboBox<>();
    private final JTextField txtRecep = new JTextField();
    private final JTextField txtVenc = new JTextField();
    private final JTextField txtCant = new JTextField();
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Disponible", "Agotado", "Vencido", "En cuarentena"});
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Código", "Materia prima", "Proveedor", "Ubicación", "Recepción", "Vence", "Cantidad", "Estado"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = -1;

    public VentanaLotesMateriaPrima() {
        setTitle("NEXOLOTE | Lotes de materia prima"); setSize(1320, 800); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Lotes de materia prima", "Recepción y control de materias primas."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Datos del lote");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Código *", txtCodigo); Estilos.agregarCampo(form, gbc, 2, 0, "Materia prima *", cbMateria); Estilos.agregarCampo(form, gbc, 4, 0, "Proveedor *", cbProv);
        Estilos.agregarCampo(form, gbc, 0, 1, "Ubicación *", cbUbi); Estilos.agregarCampo(form, gbc, 2, 1, "Recepción * (AAAA-MM-DD)", txtRecep); Estilos.agregarCampo(form, gbc, 4, 1, "Vencimiento * (AAAA-MM-DD)", txtVenc);
        Estilos.agregarCampo(form, gbc, 0, 2, "Cantidad *", txtCant); Estilos.agregarCampo(form, gbc, 2, 2, "Estado", cbEstado);
        cardForm.add(form, BorderLayout.CENTER); superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Guardar"), bActualizar = Estilos.botonActualizar("Modificar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Lotes registrados");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargarCombos(); cargar();
    }

    private void cargarCombos() {
        cargarOpciones(cbMateria, "SELECT id_materia_prima, nombre FROM MATERIA_PRIMA ORDER BY nombre");
        cargarOpciones(cbProv, "SELECT id_proveedor, nombre FROM PROVEEDOR ORDER BY nombre");
        cargarOpciones(cbUbi, "SELECT id_ubicacion, nombre FROM UBICACION ORDER BY nombre");
    }

    private void cargarOpciones(JComboBox<Estilos.Opcion> cb, String sql) {
        cb.removeAllItems();
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) cb.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT l.id_lote_mp, l.codigo_lote_mp, m.nombre, p.nombre, u.nombre, l.fecha_recepcion, l.fecha_vencimiento, l.cantidad_recibida, l.estado FROM LOTE_MATERIA_PRIMA l INNER JOIN MATERIA_PRIMA m ON l.id_materia_prima=m.id_materia_prima INNER JOIN PROVEEDOR p ON l.id_proveedor=p.id_proveedor INNER JOIN UBICACION u ON l.id_ubicacion=u.id_ubicacion ORDER BY l.id_lote_mp DESC"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDate(6), rs.getDate(7), rs.getBigDecimal(8), rs.getString(9)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() {
        if (txtCodigo.getText().trim().isEmpty() || cbMateria.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Complete los campos obligatorios."); return; }
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO LOTE_MATERIA_PRIMA (codigo_lote_mp, id_materia_prima, id_proveedor, id_ubicacion, fecha_recepcion, fecha_vencimiento, cantidad_recibida, estado) VALUES (?,?,?,?,?,?,?,?)")) { llenar(ps); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void actualizar() {
        if (idSeleccionado == -1) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE LOTE_MATERIA_PRIMA SET codigo_lote_mp=?, id_materia_prima=?, id_proveedor=?, id_ubicacion=?, fecha_recepcion=?, fecha_vencimiento=?, cantidad_recibida=?, estado=? WHERE id_lote_mp=?")) { llenar(ps); ps.setInt(9, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void llenar(PreparedStatement ps) throws SQLException {
        ps.setString(1, txtCodigo.getText().trim()); ps.setInt(2, Estilos.idDe(cbMateria));
        ps.setInt(3, Estilos.idDe(cbProv)); ps.setInt(4, Estilos.idDe(cbUbi));
        ps.setDate(5, Estilos.fechaObligatoria(txtRecep.getText(), "La fecha de recepción")); ps.setDate(6, Estilos.fechaOpcional(txtVenc.getText(), "La fecha de vencimiento"));
        ps.setBigDecimal(7, Estilos.decimal(txtCant.getText(), "La cantidad")); ps.setString(8, cbEstado.getSelectedItem().toString());
    }

    private void eliminar() {
        if (idSeleccionado == -1) return;
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el lote?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM LOTE_MATERIA_PRIMA WHERE id_lote_mp=?")) { ps.setInt(1, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow()); idSeleccionado = Integer.parseInt(modelo.getValueAt(f, 0).toString());
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT * FROM LOTE_MATERIA_PRIMA WHERE id_lote_mp=?")) { ps.setInt(1, idSeleccionado); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { txtCodigo.setText(rs.getString("codigo_lote_mp")); seleccionarOpcion(cbMateria, rs.getInt("id_materia_prima")); seleccionarOpcion(cbProv, rs.getInt("id_proveedor")); seleccionarOpcion(cbUbi, rs.getInt("id_ubicacion")); txtRecep.setText(Estilos.textoFecha(rs.getDate("fecha_recepcion"))); txtVenc.setText(Estilos.textoFecha(rs.getDate("fecha_vencimiento"))); txtCant.setText(rs.getBigDecimal("cantidad_recibida").toString()); cbEstado.setSelectedItem(rs.getString("estado")); } } } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void seleccionarOpcion(JComboBox<Estilos.Opcion> cb, int id) { for (int i = 0; i < cb.getItemCount(); i++) if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; } }
    private void limpiar() { idSeleccionado = -1; txtCodigo.setText(""); txtRecep.setText(""); txtVenc.setText(""); txtCant.setText(""); tabla.clearSelection(); }
}