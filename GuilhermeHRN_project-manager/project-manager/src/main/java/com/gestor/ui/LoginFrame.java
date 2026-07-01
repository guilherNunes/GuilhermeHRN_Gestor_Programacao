package com.gestor.ui;

import com.gestor.service.AuthService;
import javax.swing.*;
import java.awt.*;

/**
 * Esta é a "Tela de Login".
 * A primeira coisa que aparece quando você abre o programa.
 */
public class LoginFrame extends JFrame {
    private JTextField txtUser;      // Campo para digitar o nome
    private JPasswordField txtPass;  // Campo para digitar a senha (fica com asteriscos)
    private AuthService authService = new AuthService(); // Chama o "Segurança"

    public LoginFrame() {
        setTitle("Login - Gestor de Projetos");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout()); // Um jeito de organizar os campos no centro

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5); // Espaçamento entre os campos

        // Rótulo e Campo de Usuário
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1;
        txtUser = new JTextField(15);
        add(txtUser, gbc);

        // Rótulo e Campo de Senha
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        txtPass = new JPasswordField(15);
        add(txtPass, gbc);

        // Botão de Entrar
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton btnLogin = new JButton("Entrar");
        
        // O que acontece quando clica em "Entrar"
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());
            
            // Pergunta para o segurança se pode entrar
            if (authService.login(user, pass)) {
                new MainFrame().setVisible(true); // Abre a tela principal
                dispose(); // Fecha esta tela de login
            } else {
                // Se errar, mostra um aviso de erro
                JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos!", "Erro de Acesso", JOptionPane.ERROR_MESSAGE);
            }
        });
        add(btnLogin, gbc);
    }
}
