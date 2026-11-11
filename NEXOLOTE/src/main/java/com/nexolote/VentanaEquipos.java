package com.nexolote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

public class VentanaEquipos extends JFrame {
    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtTipo = new JTextField();
    private final JTextField txtMarca = new JTextField();
    private final JTextField txtModelo = new JTextField();
    private final JTextField txtFecha = new JTextField();
    private final JComboBox<Estilos.Opcion> cbUbi = new JComboBox<>();
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo", "En mantenimiento", "Fuera de servicio"});
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Código", "Nombre", "Tipo", "Marca", "Modelo", "Adquisición", "Estado", "Ubicación"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = -1;

    public VentanaEquipos() {
        setTitle("NEXOLOTE | Equipos"); setSize(1250, 760); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Equipos", "Inventario y control de maquinaria."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Datos del equipo");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Código *", txtCodigo); Estilos.agregarCampo(form, gbc, 2, 0, "Nombre *", txtNombre); Estilos.agregarCampo(form, gbc, 4, 0, "Tipo *", txtTipo);
        Estilos.agregarCampo(form, gbc, 0, 1, "Marca", txtMarca); Estilos.agregarCampo(form, gbc, 2, 1, "Modelo", txtModelo); Estilos.agregarCampo(form, gbc, 4, 1, "Adquisición", txtFecha);
        Estilos.agregarCampo(form, gbc, 0, 2, "Ubicación *", cbUbi); Estilos.agregarCampo(form, gbc, 2, 2, "Estado", cbEstado);
        cardForm.add(form, BorderLayout.CENTER); superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Guardar"), bActualizar = Estilos.botonActualizar("Actualizar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Equipos registrados");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargarCombos(); cargar();
    }

    private void cargarCombos() {
        cbUbi.removeAllItems();
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT id_ubicacion, nombre FROM UBICACION ORDER BY nombre"); ResultSet rs = ps.executeQuery()) { while (rs.next()) cbUbi.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT e.id_equipo, e.codigo_inventario, e.nombre, e.tipo_equipo, e.marca, e.modelo, e.fecha_adquisicion, e.estado, u.nombre FROM EQUIPO e LEFT JOIN UBICACION u ON e.id_ubicacion=u.id_ubicacion ORDER BY e.id_equipo DESC"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getDate(7), rs.getString(8), rs.getString(9)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() {
        if (txtCodigo.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() || txtTipo.getText().trim().isEmpty() || cbUbi.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Complete los obligatorios."); return; }
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO EQUIPO (codigo_inventario, nombre, tipo_equipo, marca, modelo, fecha_adquisicion, estado, id_ubicacion) VALUES (?,?,?,?,?,?,?,?)")) { llenar(ps); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void actualizar() {
        if (idSeleccionado == -1) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE EQUIPO SET codigo_inventario=?, nombre=?, tipo_equipo=?, marca=?, modelo=?, fecha_adquisicion=?, estado=?, id_ubicacion=? WHERE id_equipo=?")) { llenar(ps); ps.setInt(9, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void llenar(PreparedStatement ps) throws SQLException {
        ps.setString(1, txtCodigo.getText().trim()); ps.setString(2, txtNombre.getText().trim()); ps.setString(3, txtTipo.getText().trim());
        ps.setString(4, txtMarca.getText().trim()); ps.setString(5, txtModelo.getText().trim());
        ps.setDate(6, Estilos.fechaOpcional(txtFecha.getText(), "La fecha de adquisición"));
        ps.setString(7, cbEstado.getSelectedItem().toString()); ps.setInt(8, Estilos.idDe(cbUbi));
    }

    private void eliminar() {
        if (idSeleccionado == -1) return;
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el equipo?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM EQUIPO WHERE id_equipo=?")) { ps.setInt(1, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow()); idSeleccionado = Integer.parseInt(modelo.getValueAt(f, 0).toString());
        try (Connection con = Conexion.conectar()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM EQUIPO WHERE id_equipo=?")) {
                ps.setInt(1, idSeleccionado);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        txtCodigo.setText(rs.getString("codigo_inventario")); txtNombre.setText(rs.getString("nombre")); txtTipo.setText(rs.getString("tipo_equipo"));
                        txtMarca.setText(rs.getString("marca") == null ? "" : rs.getString("marca")); txtModelo.setText(rs.getString("modelo") == null ? "" : rs.getString("modelo"));
                        Date d = rs.getDate("fecha_adquisicion"); txtFecha.setText(d == null ? "" : d.toString());
                        cbEstado.setSelectedItem(rs.getString("estado")); seleccionarOpcion(cbUbi, rs.getInt("id_ubicacion"));
                    }
                }
            }
        } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void seleccionarOpcion(JComboBox<Estilos.Opcion> cb, int id) { for (int i = 0; i < cb.getItemCount(); i++) if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; } }
    private void limpiar() { idSeleccionado = -1; txtCodigo.setText(""); txtNombre.setText(""); txtTipo.setText(""); txtMarca.setText(""); txtModelo.setText(""); txtFecha.setText(""); tabla.clearSelection(); }
}