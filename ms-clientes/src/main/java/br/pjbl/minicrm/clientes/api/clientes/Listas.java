package br.pjbl.minicrm.clientes.api.clientes;

import java.util.Arrays;
import java.util.List;

final class Listas {

    private Listas() {
    }

    /** Aceita "a,b" e também o parâmetro repetido (?x=a&x=b). */
    static List<String> separar(List<String> valores) {
        if (valores == null) {
            return List.of();
        }
        return valores.stream().flatMap(v -> Arrays.stream(v.split(","))).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }
}
