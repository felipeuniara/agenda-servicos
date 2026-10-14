package br.com.agendamais.config;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Component("fmt")
public class Formatos {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter EXTENSO = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_BR);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public String moeda(Number valor) {
        if (valor == null) return "R$ 0,00";
        double v = valor.doubleValue();
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(PT_BR);
        String padrao = (v == Math.rint(v)) ? "#,##0" : "#,##0.00";
        return "R$ " + new DecimalFormat(padrao, simbolos).format(v);
    }

    public String numero(Number valor) {
        return valor == null ? "0" : String.valueOf(valor.doubleValue());
    }

    public String extenso(LocalDate data) {
        return data == null ? "" : capitalizar(data.format(EXTENSO));
    }

    public String data(LocalDate data) {
        return data == null ? "" : data.format(DATA);
    }

    public String data(LocalDateTime data) {
        return data == null ? "" : data.format(DATA);
    }

    public String hora(LocalDateTime data) {
        return data == null ? "" : data.format(HORA);
    }

    public String diaCurto(LocalDateTime data) {
        if (data == null) return "";
        String dia = data.getDayOfWeek().getDisplayName(TextStyle.SHORT, PT_BR).replace(".", "");
        return capitalizar(dia) + ", " + data.format(DIA_MES);
    }

    private static String capitalizar(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
