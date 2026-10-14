package br.com.agendamais.service;

import br.com.agendamais.dto.AgendamentoForm;
import br.com.agendamais.dto.FiltroAgenda;
import br.com.agendamais.entity.Agenda;
import br.com.agendamais.entity.Cliente;
import br.com.agendamais.entity.Servico;
import br.com.agendamais.entity.Usuario;
import br.com.agendamais.repository.AgendaRepository;
import br.com.agendamais.repository.ClienteRepository;
import br.com.agendamais.repository.ServicoRepository;
import br.com.agendamais.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgendaService {
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final AgendaRepository agendaRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;

    private final LocalTime abertura;
    private final LocalTime fechamento;
    private final int intervaloMinutos;

    public AgendaService(AgendaRepository agendaRepository, ClienteRepository clienteRepository,
                         ServicoRepository servicoRepository, UsuarioRepository usuarioRepository,
                         @Value("${agenda.horario.inicio:08:00}") String abertura,
                         @Value("${agenda.horario.fim:18:00}") String fechamento,
                         @Value("${agenda.horario.intervalo-minutos:30}") int intervaloMinutos) {
        this.agendaRepository = agendaRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.abertura = LocalTime.parse(abertura);
        this.fechamento = LocalTime.parse(fechamento);
        this.intervaloMinutos = intervaloMinutos;
    }

    public Agenda buscar(Integer id) {
        return agendaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agendamento não encontrado."));
    }

    @Transactional
    public Agenda salvar(AgendamentoForm form) {
        if (form.getClienteId() == null) {
            throw new BusinessException("clienteId", "Um agendamento deve estar associado a um cliente cadastrado.");
        }
        Cliente cliente = clienteRepository.findById(form.getClienteId())
                .orElseThrow(() -> new BusinessException("clienteId",
                        "Cliente não encontrado. Selecione um cliente cadastrado."));

        if (form.getServicoId() == null) {
            throw new BusinessException("servicoId", "Selecione um serviço.");
        }
        Servico servico = servicoRepository.findById(form.getServicoId())
                .orElseThrow(() -> new BusinessException("servicoId", "Serviço não encontrado."));

        if (form.getUsuarioId() == null) {
            throw new BusinessException("usuarioId", "Selecione o profissional responsável.");
        }
        Usuario usuario = usuarioRepository.findById(form.getUsuarioId())
                .orElseThrow(() -> new BusinessException("usuarioId", "Profissional não encontrado."));

        if (form.getData() == null || form.getHorario() == null) {
            throw new BusinessException("data", "Informe a data e o horário.");
        }
        LocalDateTime dataHora = LocalDateTime.of(form.getData(),
                form.getHorario().withSecond(0).withNano(0));

        Agenda agenda = form.getId() == null ? new Agenda() : buscar(form.getId());

        boolean dataAlterada = agenda.getId() == null || !dataHora.equals(agenda.getData());
        if (dataAlterada && dataHora.isBefore(LocalDateTime.now())) {
            throw new BusinessException("data",
                    "Não é permitido agendar para uma data ou horário que já passou.");
        }

        boolean conflito = agenda.getId() == null
                ? agendaRepository.existsByUsuarioIdAndData(usuario.getId(), dataHora)
                : agendaRepository.existsByUsuarioIdAndDataAndIdNot(usuario.getId(), dataHora, agenda.getId());
        if (conflito) {
            throw new BusinessException("horario", usuario.getNomeExibicao()
                    + " já possui um agendamento em " + dataHora.format(DATA_HORA) + ".");
        }

        agenda.setCliente(cliente);
        agenda.setServico(servico);
        agenda.setUsuario(usuario);
        agenda.setData(dataHora);
        String descricao = form.getDescricao() == null ? null : form.getDescricao().trim();
        agenda.setDescricao(descricao == null || descricao.isEmpty() ? null : descricao);
        return agendaRepository.save(agenda);
    }

    @Transactional
    public void excluir(Integer id) {
        agendaRepository.delete(buscar(id));
    }

    public List<Agenda> pesquisar(FiltroAgenda filtro) {
        if (filtro.getDe() != null && filtro.getAte() != null && filtro.getAte().isBefore(filtro.getDe())) {
            throw new BusinessException("A data final do período não pode ser anterior à data inicial.");
        }
        Specification<Agenda> spec = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (filtro.getClienteId() != null) {
                condicoes.add(cb.equal(root.get("cliente").get("id"), filtro.getClienteId()));
            }
            if (filtro.getServicoId() != null) {
                condicoes.add(cb.equal(root.get("servico").get("id"), filtro.getServicoId()));
            }
            if (filtro.getDe() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("data"),
                        filtro.getDe().atStartOfDay()));
            }
            if (filtro.getAte() != null) {
                condicoes.add(cb.lessThan(root.<LocalDateTime>get("data"),
                        filtro.getAte().plusDays(1).atStartOfDay()));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
        return agendaRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "data"));
    }

    public List<Agenda> listarPorPeriodo(String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio;
        LocalDate fim;
        switch (periodo == null ? "hoje" : periodo) {
            case "semana" -> {
                inicio = hoje.with(DayOfWeek.MONDAY);
                fim = inicio.plusDays(6);
            }
            case "mes" -> {
                inicio = hoje.withDayOfMonth(1);
                fim = hoje.withDayOfMonth(hoje.lengthOfMonth());
            }
            default -> {
                inicio = hoje;
                fim = hoje;
            }
        }
        return agendaRepository.findByDataBetweenOrderByDataAsc(inicio.atStartOfDay(), fim.atTime(23, 59, 59));
    }

    public List<LocalTime> horariosDoDia() {
        List<LocalTime> horarios = new ArrayList<>();
        LocalTime h = abertura;
        while (h.isBefore(fechamento)) {
            horarios.add(h);
            LocalTime proximo = h.plusMinutes(intervaloMinutos);
            if (!proximo.isAfter(h)) break;
            h = proximo;
        }
        return horarios;
    }
}
