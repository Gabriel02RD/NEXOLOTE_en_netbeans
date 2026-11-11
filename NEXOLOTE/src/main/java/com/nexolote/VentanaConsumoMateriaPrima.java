package com.nexolote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
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

public class VentanaConsumoMateriaPrima extends JFrame {
    private final JComboBox<Estilos.Opcion> cbLote = new JComboBox<>();
    private final JComboBox<Estilos.Opcion> cbMateria = new JComboBox<>();
    private final JTextField txtCant = new JTextField();
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Lote prod.", "Lote MP", "Materia prima", "Proveedor", "Cantidad usada"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tabla = new JTable(modelo);
    private int idLote = -1, idMateria = -1;

    public VentanaConsumoMateriaPrima() {
        setTitle("NEXOLOTE | Consumo de materia prima"); setSize(1120, 720); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Estilos.configurarVentana(this);
        add(Estilos.crearEncabezado("Consumo de materia prima", "Registro de insumos utilizados en lotes de producción."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 16)); centro.setBackground(Estilos.FONDO); centro.setBorder(BorderFactory.createEmptyBorder(18, 26, 22, 26));

        JPanel superior = new JPanel(new BorderLayout(0, 10)); superior.setOpaque(false);
        JPanel cardForm = Estilos.crearPanelFormulario("Registro de consumo");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints gbc = new GridBagConstraints();
        Estilos.agregarCampo(form, gbc, 0, 0, "Lote de producción *", cbLote); Estilos.agregarCampo(form, gbc, 2, 0, "Lote de materia prima *", cbMateria); Estilos.agregarCampo(form, gbc, 4, 0, "Cantidad utilizada *", txtCant);
        cardForm.add(form, BorderLayout.CENTER); superior.add(cardForm, BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); barra.setOpaque(false);
        JButton bGuardar = Estilos.botonGuardar("Registrar"), bActualizar = Estilos.botonActualizar("Modificar"), bEliminar = Estilos.botonEliminar("Eliminar"), bLimpiar = Estilos.botonLimpiar("Limpiar");
        bGuardar.addActionListener(e -> Estilos.ejecutar(this, this::guardar)); bActualizar.addActionListener(e -> Estilos.ejecutar(this, this::actualizar)); bEliminar.addActionListener(e -> Estilos.ejecutar(this, this::eliminar)); bLimpiar.addActionListener(e -> limpiar());
        barra.add(bGuardar); barra.add(bActualizar); barra.add(bEliminar); barra.add(bLimpiar); superior.add(barra, BorderLayout.SOUTH);
        centro.add(superior, BorderLayout.NORTH);

        JPanel cardTabla = Estilos.crearPanelTabla("Consumos registrados");
        Estilos.estilizarTabla(tabla); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) seleccionar(); });
        cardTabla.add(Estilos.crearScroll(tabla), BorderLayout.CENTER); centro.add(cardTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER); cargarCombos(); cargar();
    }

    private void cargarCombos() { cargarOp(cbLote, "SELECT id_lote, codigo_lote FROM LOTE_PRODUCCION ORDER BY codigo_lote"); cargarOp(cbMateria, "SELECT id_lote_mp, codigo_lote_mp FROM LOTE_MATERIA_PRIMA ORDER BY codigo_lote_mp"); }
    private void cargarOp(JComboBox<Estilos.Opcion> cb, String sql) { cb.removeAllItems(); try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) cb.addItem(new Estilos.Opcion(rs.getInt(1), rs.getString(2))); } } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT lp.codigo_lote, lmp.codigo_lote_mp, mp.nombre, p.nombre, c.cantidad_utilizada FROM CONSUMO_MATERIA_PRIMA c INNER JOIN LOTE_PRODUCCION lp ON c.id_lote=lp.id_lote INNER JOIN LOTE_MATERIA_PRIMA lmp ON c.id_lote_mp=lmp.id_lote_mp INNER JOIN MATERIA_PRIMA mp ON lmp.id_materia_prima=mp.id_materia_prima LEFT JOIN PROVEEDOR p ON lmp.id_proveedor=p.id_proveedor ORDER BY lp.codigo_lote"); ResultSet rs = ps.executeQuery()) { while (rs.next()) modelo.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBigDecimal(5)}); } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }

    private void guardar() { if (cbLote.getSelectedItem() == null || cbMateria.getSelectedItem() == null) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("INSERT INTO CONSUMO_MATERIA_PRIMA (id_lote, id_lote_mp, cantidad_utilizada) VALUES (?,?,?)")) { llenar(ps); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }
    private void actualizar() { if (idLote == -1 || idMateria == -1) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("UPDATE CONSUMO_MATERIA_PRIMA SET id_lote=?, id_lote_mp=?, cantidad_utilizada=? WHERE id_lote=? AND id_lote_mp=?")) { llenar(ps); ps.setInt(4, idLote); ps.setInt(5, idMateria); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }
    private void llenar(PreparedStatement ps) throws SQLException { ps.setInt(1, Estilos.idDe(cbLote)); ps.setInt(2, Estilos.idDe(cbMateria)); ps.setBigDecimal(3, Estilos.decimal(txtCant.getText(), "La cantidad")); }
    private void eliminar() { if (idLote == -1 || idMateria == -1) return; if (JOptionPane.showConfirmDialog(this, "¿Eliminar el consumo?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return; try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("DELETE FROM CONSUMO_MATERIA_PRIMA WHERE id_lote=? AND id_lote_mp=?")) { ps.setInt(1, idLote); ps.setInt(2, idMateria); ps.executeUpdate(); } limpiar(); cargar(); } catch (SQLException e) { Conexion.mostrarError(this, e); } }

    private void seleccionar() {
        int f = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        try (Connection con = Conexion.conectar()) { if (con == null) return; try (PreparedStatement ps = con.prepareStatement("SELECT c.id_lote, c.id_lote_mp, c.cantidad_utilizada FROM CONSUMO_MATERIA_PRIMA c INNER JOIN LOTE_PRODUCCION lp ON c.id_lote=lp.id_lote INNER JOIN LOTE_MATERIA_PRIMA lmp ON c.id_lote_mp=lmp.id_lote_mp WHERE lp.codigo_lote=? AND lmp.codigo_lote_mp=?")) { ps.setString(1, modelo.getValueAt(f, 0).toString()); ps.setString(2, modelo.getValueAt(f, 1).toString()); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { idLote = rs.getInt(1); idMateria = rs.getInt(2); seleccionarOp(cbLote, idLote); seleccionarOp(cbMateria, idMateria); txtCant.setText(rs.getBigDecimal(3).toString()); } } } } catch (SQLException e) { Conexion.mostrarError(this, e); }
    }
    private void seleccionarOp(JComboBox<Estilos.Opcion> cb, int id) { for (int i = 0; i < cb.getItemCount(); i++) if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; } }
    private void limpiar() { idLote = -1; idMateria = -1; txtCant.setText(""); tabla.clearSelection(); }
}