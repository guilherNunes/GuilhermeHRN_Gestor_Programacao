package com.gestor.ui;

import com.gestor.model.*;
import com.gestor.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class TaskPanel extends JPanel {
    private ProjectService projectService = new ProjectService();
    private JTable taskTable;
    private DefaultTableModel taskModel;
    private JComboBox<ProjectWrapper> cbProjects;
    
    // Checkboxes de Fases
    private JCheckBox chkReq = new JCheckBox("Requisitos (20%)");
    private JCheckBox chkDev = new JCheckBox("Desenvolvimento (60%)");
    private JCheckBox chkDep = new JCheckBox("Implantação (20%)");
    private JLabel lblPhaseDates = new JLabel("Prazos das Fases: Selecione um projeto");

    private static class ProjectWrapper {
        Project p;
        String label;
        ProjectWrapper(Project p, String label) { this.p = p; this.label = label; }
        @Override public String toString() { return label; }
    }

    public TaskPanel() {
        setLayout(new BorderLayout());
        
        // Painel Superior: Filtro e Ações
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbProjects = new JComboBox<>();
        JButton btnAdd = new JButton("Nova Tarefa");
        btnAdd.addActionListener(e -> addTask());
        
        JButton btnDelete = new JButton("Excluir Tarefa");
        btnDelete.addActionListener(e -> deleteSelectedTask());

        topPanel.add(new JLabel("Projeto:"));
        topPanel.add(cbProjects);
        topPanel.add(btnAdd);
        topPanel.add(btnDelete);
        
        // Painel de Fases (Requisito: Checkboxes de Categorias)
        JPanel phasePanel = new JPanel(new GridLayout(2, 1));
        phasePanel.setBorder(BorderFactory.createTitledBorder("Progresso das Fases do Projeto"));
        
        JPanel checkGroup = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkGroup.add(chkReq);
        checkGroup.add(chkDev);
        checkGroup.add(chkDep);
        
        phasePanel.add(checkGroup);
        phasePanel.add(lblPhaseDates);

        // Listeners para salvar o estado das fases
        chkReq.addActionListener(e -> savePhaseState());
        chkDev.addActionListener(e -> savePhaseState());
        chkDep.addActionListener(e -> savePhaseState());

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(topPanel, BorderLayout.NORTH);
        northContainer.add(phasePanel, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        // Tabela de Tarefas
        String[] columns = {"Título", "Status", "Prioridade", "Projeto"};
        taskModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        taskTable = new JTable(taskModel);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        
        cbProjects.addActionListener(e -> {
            updatePhaseUI();
            refreshTaskTable();
        });
        
        updateProjectCombo();
    }

    public void updateProjectCombo() {
        cbProjects.removeAllItems();
        cbProjects.addItem(new ProjectWrapper(null, "Selecione um Projeto..."));
        for (Project p : Database.getInstance().getProjects()) {
            cbProjects.addItem(new ProjectWrapper(p, p.getName()));
        }
    }

    private void updatePhaseUI() {
        ProjectWrapper selected = (ProjectWrapper) cbProjects.getSelectedItem();
        if (selected != null && selected.p != null) {
            Project p = selected.p;
            chkReq.setSelected(p.isRequirementsDone());
            chkDev.setSelected(p.isDevelopmentDone());
            chkDep.setSelected(p.isDeploymentDone());
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dates = String.format("<html><b>Requisitos:</b> %s | <b>Dev:</b> %s | <b>Deploy:</b> %s</html>",
                p.getRequirementsDeadline() != null ? p.getRequirementsDeadline().format(dtf) : "?",
                p.getDevelopmentDeadline() != null ? p.getDevelopmentDeadline().format(dtf) : "?",
                p.getDeploymentDeadline() != null ? p.getDeploymentDeadline().format(dtf) : "?");
            lblPhaseDates.setText(dates);
            
            chkReq.setEnabled(true);
            chkDev.setEnabled(true);
            chkDep.setEnabled(true);
        } else {
            chkReq.setSelected(false); chkDev.setSelected(false); chkDep.setSelected(false);
            chkReq.setEnabled(false); chkDev.setEnabled(false); chkDep.setEnabled(false);
            lblPhaseDates.setText("Selecione um projeto para ver os prazos das fases.");
        }
    }

    private void savePhaseState() {
        ProjectWrapper selected = (ProjectWrapper) cbProjects.getSelectedItem();
        if (selected != null && selected.p != null) {
            selected.p.setRequirementsDone(chkReq.isSelected());
            selected.p.setDevelopmentDone(chkDev.isSelected());
            selected.p.setDeploymentDone(chkDep.isSelected());
            Database.getInstance().saveProjects();
        }
    }

    private void refreshTaskTable() {
        taskModel.setRowCount(0);
        ProjectWrapper selected = (ProjectWrapper) cbProjects.getSelectedItem();
        List<Task> allTasks = Database.getInstance().getTasks();
        
        for (Task t : allTasks) {
            if (selected == null || selected.p == null || (t.getProjectId() != null && t.getProjectId().equals(selected.p.getId()))) {
                Project p = projectService.findProjectById(t.getProjectId());
                taskModel.addRow(new Object[]{
                    t.getTitle(), t.getStatus(), t.getPriority(), (p != null ? p.getName() : "N/A")
                });
            }
        }
    }

    private void addTask() {
        ProjectWrapper selected = (ProjectWrapper) cbProjects.getSelectedItem();
        if (selected == null || selected.p == null) {
            JOptionPane.showMessageDialog(this, "Selecione um projeto!");
            return;
        }
        String title = JOptionPane.showInputDialog(this, "Título da Tarefa:");
        if (title != null && !title.isEmpty()) {
            Task t = new Task(title, "", Enums.Priority.MEDIA, selected.p.getId());
            projectService.addTask(t);
            refreshTaskTable();
        }
    }

    private void deleteSelectedTask() {
        int row = taskTable.getSelectedRow();
        if (row >= 0) {
            String title = (String) taskModel.getValueAt(row, 0);
            ProjectWrapper selected = (ProjectWrapper) cbProjects.getSelectedItem();
            
            Database.getInstance().getTasks().removeIf(t -> 
                t.getTitle().equals(title) && 
                (selected == null || selected.p == null || (t.getProjectId() != null && t.getProjectId().equals(selected.p.getId())))
            );
            Database.getInstance().saveTasks();
            refreshTaskTable();
        }
    }
}
