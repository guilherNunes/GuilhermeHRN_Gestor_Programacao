package com.gestor.ui;

import com.gestor.model.*;
import com.gestor.service.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * UserPanel - Gerenciamento de Usuarios (v3.0)
 *
 * Melhorias de Usabilidade:
 * - Botao de Editar usuario adicionado (CRUD completo)
 * - Layout com cabecalho claro e separado de "Administracao"
 * - Confirmacao antes de excluir com mensagem contextual
 * - Verificacao de permissao via AuthService
 */
public class UserPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public UserPanel() {
        setLayout(new BorderLayout());
        setBackground(MainFrame.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        // Cabecalho
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(28, 32, 12, 32));
        JLabel lblTitle = new JLabel("Gerenciamento de Usuarios");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitle.setForeground(MainFrame.TEXT_DARK);
        header.add(lblTitle, BorderLayout.WEST);

        // Botoes de acao
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(MainFrame.BG_MAIN);

        JButton btnAdd = buildBtn("+ Novo Usuario", new Color(59, 130, 246), Color.WHITE);
        btnAdd.addActionListener(e -> {
            UserDialog d = new UserDialog(null, null);
            d.setVisible(true);
            if (d.isSaved()) {
                Database.getInstance().getUsers().add(d.getUser());
                Database.getInstance().saveUsers();
                refresh();
            }
        });

        JButton btnEdit = buildBtn("Editar", new Color(241, 245, 249), MainFrame.TEXT_DARK);
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Selecione um usuario para editar."); return; }
            String username = (String) model.getValueAt(row, 1);
            User u = Database.getInstance().getUsers().stream()
                    .filter(usr -> usr.getUsername().equals(username)).findFirst().orElse(null);
            if (u != null) {
                UserDialog d = new UserDialog(null, u);
                d.setVisible(true);
                if (d.isSaved()) {
                    Database.getInstance().saveUsers();
                    refresh();
                }
            }
        });

        JButton btnDelete = buildBtn("Excluir", new Color(254, 242, 242), new Color(185, 28, 28));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Selecione um usuario para excluir."); return; }
            String username = (String) model.getValueAt(row, 1);
            if ("admin".equals(username)) {
                JOptionPane.showMessageDialog(this,
                        "O usuario 'admin' padrao nao pode ser excluido.",
                        "Operacao nao permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            User current = AuthService.getCurrentUser();
            if (current == null || current.getRole() != Enums.UserRole.ADMIN) {
                JOptionPane.showMessageDialog(this,
                        "Apenas administradores podem excluir usuarios.",
                        "Permissao negada", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deseja excluir o usuario '" + username + "'?\nEsta acao nao pode ser desfeita.",
                    "Confirmar Exclusao", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                Database.getInstance().getUsers().removeIf(u -> u.getUsername().equals(username));
                Database.getInstance().saveUsers();
                refresh();
            }
        });

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);
        header.add(actions, BorderLayout.EAST);

        // Tabela
        String[] cols = {"ID", "Usuario", "Perfil"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setGridColor(new Color(226, 232, 240));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 32, 32, 32),
                new LineBorder(new Color(226, 232, 240), 1, true)));

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (User u : Database.getInstance().getUsers()) {
            model.addRow(new Object[]{u.getId().substring(0, 8), u.getUsername(), u.getRole()});
        }
    }

    private JButton buildBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atencao", JOptionPane.INFORMATION_MESSAGE);
    }
}
