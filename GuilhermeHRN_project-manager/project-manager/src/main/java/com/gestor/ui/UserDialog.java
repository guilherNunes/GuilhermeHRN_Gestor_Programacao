package com.gestor.ui;

import com.gestor.model.*;
import com.gestor.util.ValidationUtil;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * UserDialog - Formulario de Usuario (v3.0)
 *
 * Melhorias de Usabilidade:
 * - Suporte a modo de edicao (pre-preenche campos com dados existentes)
 * - Titulo do dialogo diferente para criacao vs edicao
 * - Validacao inline com mensagem de erro proxima ao campo
 * - Adicao de campo de e-mail com validacao
 * - Validacao de comprimento de campos (username, password)
 */
public class UserDialog extends JDialog {
    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);
    private final JTextField txtEmail = new JTextField(18);
    private final JComboBox<Enums.UserRole> cbRole = new JComboBox<>(Enums.UserRole.values());
    private final JLabel lblErrUser = buildErrLabel();
    private final JLabel lblErrPass = buildErrLabel();
    private final JLabel lblErrEmail = buildErrLabel();

    private boolean saved = false;
    private User user;
    private final boolean isEdit;

    public UserDialog(Frame owner, User existingUser) {
        super(owner, existingUser == null ? "Novo Usuario" : "Editar Usuario", true);
        this.user   = existingUser;
        this.isEdit = existingUser != null;
        setSize(400, 380);
        setResizable(false);
        setLocationRelativeTo(owner);
        buildUI();
        if (isEdit) populateFields();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel lblTitle = new JLabel(isEdit ? "Editar Usuario" : "Novo Usuario");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.add(lblTitle, BorderLayout.WEST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 1, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int r = 0;

        // Usuario
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; form.add(lbl("Nome de Usuario *"), gbc); r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 3; form.add(txtUser, gbc); r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 3; form.add(lblErrUser, gbc); r++;

        // Senha
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1;
        form.add(lbl(isEdit ? "Nova Senha (deixe em branco para manter)" : "Senha *"), gbc); r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 3; form.add(txtPass, gbc); r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 3; form.add(lblErrPass, gbc); r++;

        // Email
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; form.add(lbl("E-mail *"), gbc); r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 3; form.add(txtEmail, gbc); r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 3; form.add(lblErrEmail, gbc); r++;

        // Perfil
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; form.add(lbl("Perfil de Acesso"), gbc); r++;
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 3; form.add(cbRole, gbc); r++;

        // Rodape
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setBackground(new Color(241, 245, 249));
        btnCancel.setForeground(new Color(30, 41, 59));
        btnCancel.setBorderPainted(false); btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isEdit ? "Salvar Alteracoes" : "Criar Usuario");
        btnSave.setBackground(new Color(59, 130, 246));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSave.setBorderPainted(false); btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> save());

        footer.add(btnCancel);
        footer.add(btnSave);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void save() {
        lblErrUser.setText(""); lblErrPass.setText(""); lblErrEmail.setText("");
        boolean ok = true;

        String username = txtUser.getText().trim();
        if (!ValidationUtil.isNotEmpty(username)) { lblErrUser.setText("O nome de usuario e obrigatorio."); ok = false; }
        else if (!ValidationUtil.hasMaxLength(username, 20)) { lblErrUser.setText("Maximo 20 caracteres."); ok = false; }

        String pass = new String(txtPass.getPassword());
        if (!isEdit && !ValidationUtil.isNotEmpty(pass)) { lblErrPass.setText("A senha e obrigatoria para novos usuarios."); ok = false; }
        else if (ValidationUtil.isNotEmpty(pass) && !ValidationUtil.hasMaxLength(pass, 50)) { lblErrPass.setText("Maximo 50 caracteres."); ok = false; }

        String email = txtEmail.getText().trim();
        if (!ValidationUtil.isNotEmpty(email)) { lblErrEmail.setText("O e-mail e obrigatorio."); ok = false; }
        else if (!ValidationUtil.isValidEmail(email)) { lblErrEmail.setText("E-mail invalido."); ok = false; }

        if (!ok) return;

        if (user == null) user = new User();
        user.setUsername(username);
        if (ValidationUtil.isNotEmpty(pass)) user.setPassword(pass);
        user.setEmail(email);
        user.setRole((Enums.UserRole) cbRole.getSelectedItem());

        saved = true;
        dispose();
    }

    private void populateFields() {
        txtUser.setText(user.getUsername());
        txtEmail.setText(user.getEmail());
        cbRole.setSelectedItem(user.getRole());
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(71, 85, 105)); return l;
    }

    private static JLabel buildErrLabel() {
        JLabel l = new JLabel(""); l.setForeground(new Color(220, 38, 38));
        l.setFont(new Font("SansSerif", Font.PLAIN, 11)); return l;
    }

    public boolean isSaved() { return saved; }
    public User getUser() { return user; }
}
