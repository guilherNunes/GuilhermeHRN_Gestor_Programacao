package com.gestor.ui;

import com.gestor.model.*;
import com.gestor.service.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainFrame - Tela Principal Refatorada (v3.0)
 *
 * Melhorias de Usabilidade aplicadas:
 * - Layout com Sidebar (menu lateral) substituindo abas soltas
 * - Dashboard como tela inicial (visão geral imediata ao logar)
 * - Navegação clara e hierárquica: Dashboard > Projetos > Usuários
 * - Cabeçalho com informações do usuário logado e botão de logout visível
 * - Controle de acesso: menu "Usuários" só aparece para ADMIN/GERENTE
 */
public class MainFrame extends JFrame {

    // ── Cores do tema ──────────────────────────────────────────────
    static final Color SIDEBAR_BG   = new Color(30, 41, 59);
    static final Color SIDEBAR_SEL  = new Color(51, 65, 85);
    static final Color ACCENT       = new Color(59, 130, 246);
    static final Color BG_MAIN      = new Color(248, 250, 252);
    static final Color TEXT_WHITE   = Color.WHITE;
    static final Color TEXT_MUTED   = new Color(148, 163, 184);
    static final Color TEXT_DARK    = new Color(30, 41, 59);

    // ── Painéis de conteúdo ────────────────────────────────────────
    private JPanel contentArea;
    private CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private ProjectListPanel projectListPanel;
    private TaskPanel taskPanel;
    private UserPanel userPanel;

    // ── Botões do menu lateral ─────────────────────────────────────
    JButton btnDashboard, btnProjects, btnTasks, btnUsers;
    private JButton activeButton = null;

    public MainFrame() {
        setTitle("Gestor de Projetos — v3.0");
        setSize(1200, 760);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
        navigateTo("dashboard", btnDashboard);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);
    }

    // ─── Sidebar ──────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setMaximumSize(new Dimension(220, 70));
        JLabel lblLogo = new JLabel("Gestor de Projetos");
        lblLogo.setForeground(TEXT_WHITE);
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoPanel.add(lblLogo);
        sidebar.add(logoPanel);
        sidebar.add(buildSeparator());

        sidebar.add(buildSectionLabel("MENU PRINCIPAL"));

        btnDashboard = buildNavButton("  Dashboard");
        btnDashboard.addActionListener(e -> navigateTo("dashboard", btnDashboard));
        sidebar.add(btnDashboard);

        btnProjects = buildNavButton("  Projetos");
        btnProjects.addActionListener(e -> navigateTo("projects", btnProjects));
        sidebar.add(btnProjects);

        btnTasks = buildNavButton("  Tarefas");
        btnTasks.addActionListener(e -> navigateTo("tasks", btnTasks));
        sidebar.add(btnTasks);

        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && (currentUser.getRole() == Enums.UserRole.ADMIN
                || currentUser.getRole() == Enums.UserRole.GERENTE)) {
            sidebar.add(buildSectionLabel("ADMINISTRACAO"));
            btnUsers = buildNavButton("  Usuarios");
            btnUsers.addActionListener(e -> navigateTo("users", btnUsers));
            sidebar.add(btnUsers);
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildSeparator());
        sidebar.add(buildUserFooter());

        return sidebar;
    }

    private JButton buildNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(SIDEBAR_BG);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_SEL);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_BG);
            }
        });
        return btn;
    }

    private JPanel buildSectionLabel(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
        p.setBackground(SIDEBAR_BG);
        p.setMaximumSize(new Dimension(220, 32));
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        p.add(lbl);
        return p;
    }

    private JSeparator buildSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(51, 65, 85));
        sep.setMaximumSize(new Dimension(220, 1));
        return sep;
    }

    private JPanel buildUserFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBackground(SIDEBAR_BG);
        footer.setMaximumSize(new Dimension(220, 60));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        User u = AuthService.getCurrentUser();
        String username = (u != null) ? u.getUsername() : "—";
        String role     = (u != null) ? u.getRole().toString() : "";

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(SIDEBAR_BG);
        JLabel lblName = new JLabel(username);
        lblName.setForeground(TEXT_WHITE);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel lblRole = new JLabel(role);
        lblRole.setForeground(TEXT_MUTED);
        lblRole.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.add(lblName);
        info.add(lblRole);

        JButton btnLogout = new JButton("Sair");
        btnLogout.setForeground(new Color(248, 113, 113));
        btnLogout.setBackground(SIDEBAR_BG);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            AuthService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });

        footer.add(info, BorderLayout.CENTER);
        footer.add(btnLogout, BorderLayout.EAST);
        return footer;
    }

    // ─── Área de Conteúdo ─────────────────────────────────────────
    private JPanel buildContentArea() {
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(BG_MAIN);

        dashboardPanel   = new DashboardPanel(this);
        projectListPanel = new ProjectListPanel(this);
        taskPanel        = new TaskPanel();
        userPanel        = new UserPanel();

        contentArea.add(dashboardPanel,   "dashboard");
        contentArea.add(projectListPanel, "projects");
        contentArea.add(taskPanel,        "tasks");
        contentArea.add(userPanel,        "users");

        return contentArea;
    }

    // ══════════════════════════════════════════════════════════════
    //  Navegação pública
    // ══════════════════════════════════════════════════════════════
    public void navigateTo(String card, JButton button) {
        cardLayout.show(contentArea, card);

        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_BG);
            activeButton.setForeground(TEXT_MUTED);
        }
        activeButton = button;
        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_SEL);
            activeButton.setForeground(TEXT_WHITE);
        }

        if ("dashboard".equals(card)) dashboardPanel.refresh();
        if ("projects".equals(card))  projectListPanel.refresh();
        if ("tasks".equals(card))     taskPanel.updateProjectCombo();
        if ("users".equals(card))     userPanel.refresh();
    }

    public void openProjectDetail(Project project) {
        for (Component c : contentArea.getComponents()) {
            if (c instanceof ProjectDetailPanel) {
                contentArea.remove(c);
            }
        }
        ProjectDetailPanel detail = new ProjectDetailPanel(this, project);
        contentArea.add(detail, "project_detail");
        cardLayout.show(contentArea, "project_detail");

        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_BG);
            activeButton.setForeground(TEXT_MUTED);
        }
        activeButton = btnProjects;
        if (btnProjects != null) {
            btnProjects.setBackground(SIDEBAR_SEL);
            btnProjects.setForeground(TEXT_WHITE);
        }
    }
}
