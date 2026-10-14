package br.com.agendamais.service;

import br.com.agendamais.dto.AgendamentoForm;
import br.com.agendamais.dto.FiltroAgenda;
import br.com.agendamais.dto.UsuarioForm;
import br.com.agendamais.entity.*;
import br.com.agendamais.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class RegrasDeNegocioTest {
    @Autowired AgendaService agendaService;
    @Autowired ServicoService servicoService;
    @Autowired ClienteService clienteService;
    @Autowired UsuarioService usuarioService;
    @Autowired ClienteRepository clienteRepository;
    @Autowired ServicoRepository servicoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired TipoAcessoRepository tipoAcessoRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private AgendamentoForm agendamentoValido() {
        AgendamentoForm f = new AgendamentoForm();
        f.setClienteId(clienteRepository.findAll().get(0).getId());
        f.setServicoId(servicoRepository.findAll().get(0).getId());
        f.setUsuarioId(usuarioRepository.findByUsuarioIgnoreCase("marina").orElseThrow().getId());
        f.setData(LocalDate.now().plusDays(20));
        f.setHorario(LocalTime.of(10, 0));
        return f;
    }

    @Test
    void rn01_naoPermiteAgendamentoNoPassado() {
        AgendamentoForm f = agendamentoValido();
        f.setData(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> agendaService.salvar(f))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já passou");
    }

    @Test
    void rn01_permiteAgendamentoFuturo() {
        Agenda salvo = agendaService.salvar(agendamentoValido());
        assertThat(salvo.getId()).isNotNull();
    }

    @Test
    void rn02_mesmoUsuarioNaoPodeTerDoisAgendamentosNoMesmoHorario() {
        agendaService.salvar(agendamentoValido());
        AgendamentoForm conflito = agendamentoValido();
        assertThatThrownBy(() -> agendaService.salvar(conflito))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já possui um agendamento");
    }

    @Test
    void rn02_outroProfissionalPodeUsarOMesmoHorario() {
        agendaService.salvar(agendamentoValido());
        AgendamentoForm outro = agendamentoValido();
        outro.setUsuarioId(usuarioRepository.findByUsuarioIgnoreCase("juliana").orElseThrow().getId());
        assertThat(agendaService.salvar(outro).getId()).isNotNull();
    }

    @Test
    void rn03_valorDoServicoDeveSerMaiorQueZero() {
        assertThatThrownBy(() -> servicoService.salvar(new Servico("Grátis", 0.0)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("maior que zero");
        assertThatThrownBy(() -> servicoService.salvar(new Servico("Negativo", -10.0)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rn04_agendamentoExigeClienteCadastrado() {
        AgendamentoForm semCliente = agendamentoValido();
        semCliente.setClienteId(null);
        assertThatThrownBy(() -> agendaService.salvar(semCliente))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cliente");

        AgendamentoForm clienteInexistente = agendamentoValido();
        clienteInexistente.setClienteId(99999);
        assertThatThrownBy(() -> agendaService.salvar(clienteInexistente))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rn05_naoExcluiServicoVinculadoAAgendamento() {
        Servico servico = servicoRepository.findAll().get(0);
        agendaService.salvar(agendamentoValido());
        assertThatThrownBy(() -> servicoService.excluir(servico.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vinculado");
    }

    @Test
    void rn06_naoExcluiClienteComAgendamentos() {
        Cliente cliente = clienteRepository.findAll().get(0);
        agendaService.salvar(agendamentoValido());
        assertThatThrownBy(() -> clienteService.excluir(cliente.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("agendamento");
    }

    @Test
    void rn07_emailInvalidoOuDuplicadoEhRecusado() {
        Cliente invalido = novoCliente("email-sem-arroba");
        assertThatThrownBy(() -> clienteService.salvar(invalido))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("válido");

        String existente = clienteRepository.findAll().get(0).getEmail();
        Cliente duplicado = novoCliente(existente.toUpperCase());
        assertThatThrownBy(() -> clienteService.salvar(duplicado))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe");
    }

    @Test
    void rn08_usuarioPrecisaDeTipoDeAcessoValido() {
        UsuarioForm form = new UsuarioForm();
        form.setUsuario("teste");
        form.setSenha("123456");
        form.setTipoAcessoId(99999);
        assertThatThrownBy(() -> usuarioService.salvar(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de acesso");
    }

    @Test
    void rn09_senhaGravadaComBCrypt() {
        UsuarioForm form = new UsuarioForm();
        form.setUsuario("novo.usuario");
        form.setSenha("minhaSenha1");
        form.setTipoAcessoId(tipoAcessoRepository.findAll().get(0).getId());
        Usuario salvo = usuarioService.salvar(form);

        assertThat(salvo.getSenha()).isNotEqualTo("minhaSenha1").startsWith("$2");
        assertThat(passwordEncoder.matches("minhaSenha1", salvo.getSenha())).isTrue();
    }

    @Test
    void rn10_pesquisaPorClienteServicoEPeriodo() {
        Agenda salvo = agendaService.salvar(agendamentoValido());

        FiltroAgenda porCliente = new FiltroAgenda();
        porCliente.setClienteId(salvo.getCliente().getId());
        assertThat(agendaService.pesquisar(porCliente))
                .allMatch(a -> a.getCliente().getId().equals(salvo.getCliente().getId()));

        FiltroAgenda porServico = new FiltroAgenda();
        porServico.setServicoId(salvo.getServico().getId());
        assertThat(agendaService.pesquisar(porServico))
                .allMatch(a -> a.getServico().getId().equals(salvo.getServico().getId()));

        FiltroAgenda porPeriodo = new FiltroAgenda();
        porPeriodo.setDe(salvo.getData().toLocalDate());
        porPeriodo.setAte(salvo.getData().toLocalDate());
        List<Agenda> resultado = agendaService.pesquisar(porPeriodo);
        assertThat(resultado).extracting(Agenda::getId).contains(salvo.getId());
        assertThat(resultado).allMatch(a -> a.getData().toLocalDate().equals(salvo.getData().toLocalDate()));
    }

    private Cliente novoCliente(String email) {
        Cliente c = new Cliente();
        c.setNome("Cliente Teste");
        c.setTelefone("(11) 90000-0000");
        c.setEmail(email);
        return c;
    }
}
