package br.com.agendamais.service;

public class BusinessException extends RuntimeException {
    private final String campo;

    public BusinessException(String mensagem) {
        this(null, mensagem);
    }

    public BusinessException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
