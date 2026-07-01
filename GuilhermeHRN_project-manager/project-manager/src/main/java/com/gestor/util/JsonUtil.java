package com.gestor.util;

import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static final String DATA_DIR = System.getProperty("user.home") + File.separator + ".gestor_projetos" + File.separator + "data";
    
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> 
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> 
                LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

    private static void ensureDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    public static <T> void saveList(List<T> list, String filename) {
        ensureDirectory();
        try (Writer writer = new FileWriter(new File(DATA_DIR, filename))) {
            gson.toJson(list, writer);
            System.out.println("Salvo com sucesso: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> loadList(String filename, Type type) {
        ensureDirectory();
        File file = new File(DATA_DIR, filename);
        if (!file.exists()) {
            System.out.println("Arquivo não encontrado, iniciando vazio: " + filename);
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            List<T> list = gson.fromJson(reader, type);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
