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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class VentanaMantenimientos extends JFrame {
    private final JComboBox<Estilos.Opcion> cbEquipo = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbResp = new JComboBox<>();
    private final JTextField txtFProg = new JTextField();
    private final JTextField txtFReal = new JTextField();
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Preventivo", "Correctivo", "Predictivo", "Otro"});
    private final JTextField txtCosto = new JTextField("0");
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Programado", "En proceso", "Realizado", "Cancelado"});
    private final JTextArea txtDesc = new JTextArea(2, 20);
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Equipo", "Responsable", "Prog.", "Real.", "Tipo", "Costo", "Estado"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idSeleccionado = -1;

    public VentanaMantenimientos() {
        setTitle("NEXOLOTE | Mantenimientos"); setSize(1250, 740); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Mantenimientos", "Control de mantenimiento de equipos."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Datos del mantenimiento");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Equipo *", cbEquipo); Estilos.agregarCampo(form, gbc, 2, 0, "Responsable *", cbResp);
        Estilos.agregarCampo(form, gbc, 0, 1, "F. Programada *", txtFProg); Estilos.agregarCampo(form, gbc, 2, 1, "F. Realización", txtFReal);
        Estilos.agregarCampo(form, gbc, 0, 2, "Tipo", cbTipo); Estilos.agregarCampo(form, gbc, 2, 2, "Costo", txtCosto); Estilos.agregarCampo(form, gbc, 4, 2, "Estado", cbEstado);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.insets = new java.awt.Insets(5, 5, 5, 8);
        form.add(Estilos.crearEtiqueta("Descripción"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 1; gbc.insets = new java.awt.Insets(5, 5, 5, 14);
        Estilos.estilizarAreaTexto(txtDesc); form.add(new JScrollPane(txtDesc), gbc);
        cardForm.add(form, BorderLayout.CENTER); superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Registrar"), bActualizar = Estilos.botonActualizar("Modificar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Historial de mantenimientos");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargarCombos(); cargar();
    }

    private void cargarCombos() { cargarOp(cbEquipo, "SELECT id_equipo, CONCAT(codigo_inventario,' - ',nombre) FROM EQUIPO ORDER BY nombre"); cargarOp(cbResp, "SELECT id_responsable, CONCAT(nombres,' ',apellidos) FROM RESPONSABLE WHERE estado='Activo' ORDER BY nombres"); }
    private void cargarOp(JComboBox<Estilos.Opcion> cb, String sql) { cb.removeAllItems(); try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) cb.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT m.id_mantenimiento, e.codigo_inventario, e.nombre, CONCAT(r.nombres,' ',r.apellidos), m.fecha_programada, m.fecha_realizacion, m.tipo_mantenimiento, m.costo, m.estado FROM MANTENIMIENTO m INNER JOIN EQUIPO e ON m.id_equipo=e.id_equipo INNER JOIN RESPONSABLE r ON m.id_responsable=r.id_responsable ORDER BY m.id_mantenimiento DESC"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getInt(1), rs.getString(2) + " - " + rs.getString(3), rs.getString(4), rs.getDate(5), rs.getDate(6), rs.getString(7), rs.getBigDecimal(8), rs.getString(9)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() { if (cbEquipo.getSelectedItem() == null || cbResp.getSelectedItem() == null || txtFProg.getText().trim().isEmpty()) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO MANTENIMIENTO (id_equipo, id_responsable, fecha_programada, fecha_realizacion, tipo_mantenimiento, descripcion, costo, estado) VALUES (?,?,?,?,?,?,?,?)")) { llenar(ps); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }
    private void actualizar() { if (idSeleccionado == -1) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE MANTENIMIENTO SET id_equipo=?, id_responsable=?, fecha_programada=?, fecha_realizacion=?, tipo_mantenimiento=?, descripcion=?, costo=?, estado=? WHERE id_mantenimiento=?")) { llenar(ps); ps.setInt(9, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void llenar(PreparedStatement ps) throws SQLException {
        ps.setInt(1, Estilos.idDe(cbEquipo)); ps.setInt(2, Estilos.idDe(cbResp));
        ps.setDate(3, Estilos.fechaObligatoria(txtFProg.getText(), "La fecha programada"));
        ps.setDate(4, Estilos.fechaOpcional(txtFReal.getText(), "La fecha de realización"));
        ps.setString(5, cbTipo.getSelectedItem().toString()); ps.setString(6, txtDesc.getText().trim());
        ps.setBigDecimal(7, Estilos.decimal(txtCosto.getText(), "El costo")); ps.setString(8, cbEstado.getSelectedItem().toString());
    }

    private void eliminar() { if (idSeleccionado == -1) return; if (JOptionPane.showConfirmDialog(this, "¿Eliminar el mantenimiento?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM MANTENIMIENTO WHERE id_mantenimiento=?")) { ps.setInt(1, idSeleccionado); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow()); idSeleccionado = Integer.parseInt(modelo.getValueAt(f, 0).toString());
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT * FROM MANTENIMIENTO WHERE id_mantenimiento=?")) { ps.setInt(1, idSeleccionado); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { seleccionarOp(cbEquipo, rs.getInt("id_equipo")); seleccionarOp(cbResp, rs.getInt("id_responsable")); txtFProg.setText(rs.getDate("fecha_programada").toString()); Date fr = rs.getDate("fecha_realizacion"); txtFReal.setText(fr == null ? "" : fr.toString()); cbTipo.setSelectedItem(rs.getString("tipo_mantenimiento")); txtDesc.setText(rs.getString("descripcion") == null ? "" : rs.getString("descripcion")); BigDecimal c = rs.getBigDecimal("costo"); txtCosto.setText(c == null ? "0" : c.toPlainString()); cbEstado.setSelectedItem(rs.getString("estado")); } } } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }
    private void seleccionarOp(JComboBox<Estilos.Opcion> cb, int id) { for (int i = 0; i < cb.getItemCount(); i++) if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; } }
    private void limpiar() { idSeleccionado = -1; txtFProg.setText(""); txtFReal.setText(""); txtDesc.setText(""); txtCosto.setText("0"); tabla.clearSelection(); }
}