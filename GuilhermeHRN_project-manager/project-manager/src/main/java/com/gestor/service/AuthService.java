package com.gestor.service;

import com.gestor.model.User;
import com.gestor.model.Enums;
import java.util.List;

/**
 * Servico de Autenticacao e Autorizacao.
 * Requisito: Persistencia em JSON e controle de acesso.
 */
public class AuthService {
    private static User currentUser;

    public boolean login(String username, String password) {
        List<User> users = Database.getInstance().getUsers();
        for (User u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                currentUser = u;
                Database.getInstance().log(Enums.LogAction.LOGIN, "Usuario " + username + " entrou no sistema.");
                return true;
            }
        }
        return false;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public boolean hasPermission(Enums.UserRole requiredRole) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Enums.UserRole.ADMIN) return true;
        if (currentUser.getRole() == Enums.UserRole.GERENTE && requiredRole != Enums.UserRole.ADMIN) return true;
        return currentUser.getRole() == requiredRole;
    }
}
