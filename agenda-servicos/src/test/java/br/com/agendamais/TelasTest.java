package br.com.agendamais;

import br.com.agendamais.config.LoginInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TelasTest {
    @Autowired MockMvc mvc;

    @Test
    void loginComUsuarioDeExemplo() throws Exception {
        mvc.perform(post("/login").param("usuario", "marina").param("senha", "123456"))
                .andExpect(redirectedUrl("/agenda"))
                .andExpect(request().sessionAttribute(LoginInterceptor.USUARIO_SESSAO, 1));
    }

    @Test
    void loginComSenhaErrada() throws Exception {
        mvc.perform(post("/login").param("usuario", "marina").param("senha", "errada"))
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void semLoginRedirecionaParaLogin() throws Exception {
        mvc.perform(get("/agenda")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void todasAsTelasAbrem() throws Exception {
        for (String url : new String[]{"/agenda", "/agenda?periodo=semana", "/agenda?periodo=mes",
                "/agenda?clienteId=1", "/agenda/novo", "/agenda/1/editar", "/clientes",
                "/clientes?q=ana&filtro=completo&ordem=nome", "/clientes/novo", "/clientes/1/editar",
                "/servicos", "/servicos/novo", "/servicos/1/editar", "/usuarios", "/usuarios/novo",
                "/usuarios/1/editar", "/tipos-acesso", "/tipos-acesso/novo", "/enderecos", "/enderecos/novo",
                "/enderecos/1/editar"}) {
            mvc.perform(get(url).sessionAttr(LoginInterceptor.USUARIO_SESSAO, 1)).andExpect(status().isOk());
        }
    }
}
