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

public class VentanaLotesProduccion extends JFrame {
    private final JTextField txtCodigo = new JTextField();
    private final JComboBox<Estilos.Opcion> cbProd = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbResp = new JComboBox<>();
    private final JTextField txtFProd = new JTextField();
    private final JTextField txtFVenc = new JTextField();
    private final JTextField txtCant = new JTextField();
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"En producción", "Terminado", "En cuarentena", "Rechazado"});
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Código", "Producto", "Responsable", "Producción", "Vence", "Cantidad", "Estado"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = -1;

    public VentanaLotesProduccion() {
        setTitle("NEXOLOTE | Lotes de producción"); setSize(1220, 760); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Lotes de producción", "Registro y seguimiento de fabricación."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Datos del lote");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Código *", txtCodigo); Estilos.agregarCampo(form, gbc, 2, 0, "Producto *", cbProd); Estilos.agregarCampo(form, gbc, 4, 0, "Responsable *", cbResp);
        Estilos.agregarCampo(form, gbc, 0, 1, "F. Producción *", txtFProd); Estilos.agregarCampo(form, gbc, 2, 1, "F. Vencimiento *", txtFVenc); Estilos.agregarCampo(form, gbc, 4, 1, "Cantidad *", txtCant);
        Estilos.agregarCampo(form, gbc, 0, 2, "Estado", cbEstado);
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
        cargarOp(cbProd, "SELECT id_producto, nombre FROM PRODUCTO ORDER BY nombre");
        cargarOp(cbResp, "SELECT id_responsable, CONCAT(nombres,' ',apellidos) FROM RESPONSABLE WHERE estado='Activo' ORDER BY nombres");
    }

    private void cargarOp(JComboBox<Estilos.Opcion> cb, String sql) {
        cb.removeAllItems();
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) cb.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT l.id_lote, l.codigo_lote, p.nombre, CONCAT(r.nombres,' ',r.apellidos), l.fecha_produccion, l.fecha_vencimiento, l.cantidad_producida, l.estado FROM LOTE_PRODUCCION l INNER JOIN PRODUCTO p ON l.id_producto=p.id_producto INNER JOIN RESPONSABLE r ON l.id_responsable=r.id_responsable ORDER BY l.id_lote DESC"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDate(5), rs.getDate(6), rs.getBigDecimal(7), rs.getString(8)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() {
        if (txtCodigo.getText().trim().isEmpty() || cbProd.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Complete los campos obligatorios."); return; }
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO LOTE_PRODUCCION (codigo_lote, id_producto, id_responsable, fecha_produccion, fecha_vencimiento, cantidad_producida, estado) VALUES (?,?,?,?,?,?,?)")) { llenar(ps); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void actualizar() {
        if (idSeleccionado == -1) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE LOTE_PRODUCCION SET codigo_lote=?, id_producto=?, id_responsable=?, fecha_produccion=?, fecha_vencimiento=?, cantidad_producida=?, estado=? WHERE id_lote=?")) { llenar(ps); ps.setInt(8, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void llenar(PreparedStatement ps) throws SQLException {
        ps.setString(1, txtCodigo.getText().trim()); ps.setInt(2, Estilos.idDe(cbProd)); ps.setInt(3, Estilos.idDe(cbResp));
        ps.setDate(4, Estilos.fechaObligatoria(txtFProd.getText(), "La fecha de producción")); ps.setDate(5, Estilos.fechaOpcional(txtFVenc.getText(), "La fecha de vencimiento"));
        ps.setBigDecimal(6, Estilos.decimal(txtCant.getText(), "La cantidad")); ps.setString(7, cbEstado.getSelectedItem().toString());
    }

    private void eliminar() {
        if (idSeleccionado == -1) return;
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el lote?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM LOTE_PRODUCCION WHERE id_lote=?")) { ps.setInt(1, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow()); idSeleccionado = Integer.parseInt(modelo.getValueAt(f, 0).toString());
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT * FROM LOTE_PRODUCCION WHERE id_lote=?")) { ps.setInt(1, idSeleccionado); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { txtCodigo.setText(rs.getString("codigo_lote")); seleccionarOpcion(cbProd, rs.getInt("id_producto")); seleccionarOpcion(cbResp, rs.getInt("id_responsable")); txtFProd.setText(Estilos.textoFecha(rs.getDate("fecha_produccion"))); txtFVenc.setText(Estilos.textoFecha(rs.getDate("fecha_vencimiento"))); txtCant.setText(rs.getBigDecimal("cantidad_producida").toString()); cbEstado.setSelectedItem(rs.getString("estado")); } } } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void seleccionarOpcion(JComboBox<Estilos.Opcion> cb, int id) { for (int i = 0; i < cb.getItemCount(); i++) if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; } }
    private void limpiar() { idSeleccionado = -1; txtCodigo.setText(""); txtFProd.setText(""); txtFVenc.setText(""); txtCant.setText(""); tabla.clearSelection(); }
}