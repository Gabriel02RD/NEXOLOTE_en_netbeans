package com.nexolote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class VentanaProveedores extends JFrame {
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtId = new JTextField();
    private final JTextField txtTel = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtDir = new JTextField();
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Nombre", "Identificación", "Teléfono", "Correo", "Dirección"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = -1;

    public VentanaProveedores() {
        setTitle("NEXOLOTE | Proveedores"); setSize(1120, 720); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Proveedores", "Administra el registro de proveedores."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Información del proveedor");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Nombre *", txtNombre); Estilos.agregarCampo(form, gbc, 2, 0, "Identificación *", txtId);
        Estilos.agregarCampo(form, gbc, 0, 1, "Teléfono", txtTel); Estilos.agregarCampo(form, gbc, 2, 1, "Correo", txtCorreo);
        Estilos.agregarCampo(form, gbc, 0, 2, "Dirección", txtDir);
        cardForm.add(form, BorderLayout.CENTER); superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Guardar"), bActualizar = Estilos.botonActualizar("Actualizar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Proveedores registrados");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargar();
    }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT id_proveedor, nombre, identificacion, telefono, correo, direccion FROM PROVEEDOR ORDER BY id_proveedor"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() {
        if (txtNombre.getText().trim().isEmpty() || txtId.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Nombre e identificación son obligatorios."); return; }
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO PROVEEDOR (nombre, identificacion, telefono, correo, direccion) VALUES (?,?,?,?,?)")) { ps.setString(1, txtNombre.getText().trim()); ps.setString(2, txtId.getText().trim()); ps.setString(3, txtTel.getText().trim()); ps.setString(4, txtCorreo.getText().trim()); ps.setString(5, txtDir.getText().trim()); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void actualizar() {
        if (idSeleccionado == -1) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE PROVEEDOR SET nombre=?, identificacion=?, telefono=?, correo=?, direccion=? WHERE id_proveedor=?")) { ps.setString(1, txtNombre.getText().trim()); ps.setString(2, txtId.getText().trim()); ps.setString(3, txtTel.getText().trim()); ps.setString(4, txtCorreo.getText().trim()); ps.setString(5, txtDir.getText().trim()); ps.setInt(6, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void eliminar() {
        if (idSeleccionado == -1) return;
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el proveedor?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM PROVEEDOR WHERE id_proveedor=?")) { ps.setInt(1, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow()); idSeleccionado = Integer.parseInt(modelo.getValueAt(f, 0).toString());
        txtNombre.setText(modelo.getValueAt(f, 1).toString()); txtId.setText(modelo.getValueAt(f, 2).toString());
        txtTel.setText(modelo.getValueAt(f, 3) == null ? "" : modelo.getValueAt(f, 3).toString()); txtCorreo.setText(modelo.getValueAt(f, 4) == null ? "" : modelo.getValueAt(f, 4).toString());
        txtDir.setText(modelo.getValueAt(f, 5) == null ? "" : modelo.getValueAt(f, 5).toString());
    }

    private void limpiar() { idSeleccionado = -1; txtNombre.setText(""); txtId.setText(""); txtTel.setText(""); txtCorreo.setText(""); txtDir.setText(""); tabla.clearSelection(); }
}