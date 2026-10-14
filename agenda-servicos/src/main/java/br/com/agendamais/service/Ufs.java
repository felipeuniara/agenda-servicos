package br.com.agendamais.service;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component("ufs")
public class Ufs {
    private static final Map<String, String> UFS;

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("AC", "Acre");
        m.put("AL", "Alagoas");
        m.put("AP", "Amapá");
        m.put("AM", "Amazonas");
        m.put("BA", "Bahia");
        m.put("CE", "Ceará");
        m.put("DF", "Distrito Federal");
        m.put("ES", "Espírito Santo");
        m.put("GO", "Goiás");
        m.put("MA", "Maranhão");
        m.put("MT", "Mato Grosso");
        m.put("MS", "Mato Grosso do Sul");
        m.put("MG", "Minas Gerais");
        m.put("PA", "Pará");
        m.put("PB", "Paraíba");
        m.put("PR", "Paraná");
        m.put("PE", "Pernambuco");
        m.put("PI", "Piauí");
        m.put("RJ", "Rio de Janeiro");
        m.put("RN", "Rio Grande do Norte");
        m.put("RS", "Rio Grande do Sul");
        m.put("RO", "Rondônia");
        m.put("RR", "Roraima");
        m.put("SC", "Santa Catarina");
        m.put("SP", "São Paulo");
        m.put("SE", "Sergipe");
        m.put("TO", "Tocantins");
        UFS = Collections.unmodifiableMap(m);
    }

    public Map<String, String> getTodas() {
        return UFS;
    }

    public static boolean valida(String uf) {
        return uf != null && UFS.containsKey(uf.toUpperCase());
    }

    public static String estado(String uf) {
        return uf == null ? null : UFS.get(uf.toUpperCase());
    }
}
