package com.nexolote;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
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

public class VentanaProductos extends JFrame {
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtDescripcion = new JTextField();
    private final JTextField txtUnidad = new JTextField();
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Nombre", "Descripción", "Unidad", "Estado"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = -1;

    public VentanaProductos() {
        setTitle("NEXOLOTE | Gestión de productos"); setSize(1120, 720); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Gestión de productos", "Registra, consulta y administra el catálogo de productos."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));
        
        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Información del producto");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Nombre *", txtNombre); Estilos.agregarCampo(form, gbc, 2, 0, "Unidad de medida *", txtUnidad);
        Estilos.agregarCampo(form, gbc, 0, 1, "Descripción", txtDescripcion); Estilos.agregarCampo(form, gbc, 2, 1, "Estado", cbEstado);
        cardForm.add(form, BorderLayout.CENTER);
        superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Guardar"), bActualizar = Estilos.botonActualizar("Actualizar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Productos registrados");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargar();
    }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT id_producto, nombre, descripcion, unidad_medida, estado FROM PRODUCTO ORDER BY id_producto"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() {
        if (txtNombre.getText().trim().isEmpty() || txtUnidad.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Nombre y unidad son obligatorios."); return; }
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO PRODUCTO (nombre, descripcion, unidad_medida, estado) VALUES (?,?,?,?)")) { ps.setString(1, txtNombre.getText().trim()); ps.setString(2, txtDescripcion.getText().trim()); ps.setString(3, txtUnidad.getText().trim()); ps.setString(4, cbEstado.getSelectedItem().toString()); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void actualizar() {
        if (idSeleccionado == -1) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE PRODUCTO SET nombre=?, descripcion=?, unidad_medida=?, estado=? WHERE id_producto=?")) { ps.setString(1, txtNombre.getText().trim()); ps.setString(2, txtDescripcion.getText().trim()); ps.setString(3, txtUnidad.getText().trim()); ps.setString(4, cbEstado.getSelectedItem().toString()); ps.setInt(5, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void eliminar() {
        if (idSeleccionado == -1) return;
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el producto?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM PRODUCTO WHERE id_producto=?")) { ps.setInt(1, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow()); idSeleccionado = Integer.parseInt(modelo.getValueAt(f, 0).toString());
        txtNombre.setText(modelo.getValueAt(f, 1).toString()); txtDescripcion.setText(modelo.getValueAt(f, 2) == null ? "" : modelo.getValueAt(f, 2).toString());
        txtUnidad.setText(modelo.getValueAt(f, 3).toString()); cbEstado.setSelectedItem(modelo.getValueAt(f, 4).toString());
    }

    private void limpiar() { idSeleccionado = -1; txtNombre.setText(""); txtDescripcion.setText(""); txtUnidad.setText(""); cbEstado.setSelectedIndex(0); tabla.clearSelection(); }
}