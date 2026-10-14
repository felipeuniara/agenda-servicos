package br.com.agendamais.config;

import br.com.agendamais.entity.*;
import br.com.agendamais.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DadosIniciais implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private final TipoAcessoRepository tipoAcessoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;
    private final AgendaRepository agendaRepository;
    private final PasswordEncoder passwordEncoder;

    public DadosIniciais(TipoAcessoRepository tipoAcessoRepository, UsuarioRepository usuarioRepository,
                         ServicoRepository servicoRepository, ClienteRepository clienteRepository,
                         AgendaRepository agendaRepository, PasswordEncoder passwordEncoder) {
        this.tipoAcessoRepository = tipoAcessoRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.agendaRepository = agendaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (tipoAcessoRepository.count() > 0) {
            return;
        }
        log.info("Banco vazio: criando dados de exemplo do Agenda+");

        TipoAcesso admin = tipoAcessoRepository.save(new TipoAcesso("Administrador"));
        TipoAcesso profissional = tipoAcessoRepository.save(new TipoAcesso("Profissional"));

        Usuario marina = usuario("marina", admin);
        Usuario juliana = usuario("juliana", profissional);

        Servico corte = servicoRepository.save(new Servico("Corte feminino", 120.0));
        Servico coloracao = servicoRepository.save(new Servico("Coloração", 280.0));
        Servico manicure = servicoRepository.save(new Servico("Manicure", 130.0));
        Servico escova = servicoRepository.save(new Servico("Escova", 90.0));
        servicoRepository.save(new Servico("Pedicure", 70.0));

        Cliente ana = cliente("Ana Martins", "(11) 99912-3401", "ana.martins@email.com",
                endereco("Rua das Flores", 120, null, "01310-100", "São Paulo", "SP"));
        Cliente beatriz = cliente("Beatriz Souza", "(11) 98834-7720", "bia.souza@email.com",
                endereco("Av. Paulista", 980, "Apto 52", "01310-200", "São Paulo", "SP"));
        Cliente carla = cliente("Carla Lima", "(11) 97718-5526", "carla.lima@email.com",
                endereco("Rua do Comércio", 44, null, "13010-050", "Campinas", "SP"));
        Cliente daniel = cliente("Daniel Rocha", "(19) 99670-1182", "daniel.rocha@email.com",
                endereco("Rua Bela Vista", 305, null, "13015-300", "Campinas", "SP"));
        Cliente eduarda = cliente("Eduarda Alves", "(11) 95555-1020", "eduarda.alves@email.com",
                endereco(null, null, null, null, "São Paulo", "SP"));

        LocalDate hoje = LocalDate.now();
        agenda(hoje, "09:00", ana, corte, marina, null);
        agenda(hoje, "11:30", beatriz, coloracao, marina, "Cliente prefere tons mais frios.");
        agenda(hoje, "15:00", carla, manicure, juliana, null);
        agenda(hoje.plusDays(1), "10:00", daniel, escova, juliana, null);
        agenda(hoje.plusDays(3), "14:00", ana, manicure, juliana, null);
        agenda(hoje.plusDays(8), "09:30", eduarda, corte, marina, "Primeira visita.");
    }

    private Usuario usuario(String login, TipoAcesso tipo) {
        Usuario u = new Usuario();
        u.setUsuario(login);
        u.setSenha(passwordEncoder.encode("123456"));
        u.setDtCadastro(LocalDate.now());
        u.setTipoAcesso(tipo);
        return usuarioRepository.save(u);
    }

    private Endereco endereco(String logradouro, Integer numero, String complemento, String cep,
                              String cidade, String uf) {
        Endereco e = new Endereco();
        e.setLogradouro(logradouro);
        e.setNumero(numero);
        e.setComplemento(complemento);
        e.setCep(cep);
        e.setCidade(cidade);
        e.setUf(uf);
        e.setEstado("SP".equals(uf) ? "São Paulo" : null);
        return e;
    }

    private Cliente cliente(String nome, String telefone, String email, Endereco endereco) {
        Cliente c = new Cliente();
        c.setNome(nome);
        c.setTelefone(telefone);
        c.setEmail(email);
        c.setEndereco(endereco);
        return clienteRepository.save(c);
    }

    private void agenda(LocalDate dia, String hora, Cliente cliente, Servico servico, Usuario usuario,
                        String descricao) {
        Agenda a = new Agenda();
        a.setData(dia.atTime(LocalTime.parse(hora)));
        a.setCliente(cliente);
        a.setServico(servico);
        a.setUsuario(usuario);
        a.setDescricao(descricao);
        agendaRepository.save(a);
    }
}
