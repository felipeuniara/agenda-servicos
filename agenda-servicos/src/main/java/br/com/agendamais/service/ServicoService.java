package br.com.agendamais.service;

import br.com.agendamais.entity.Servico;
import br.com.agendamais.repository.AgendaRepository;
import br.com.agendamais.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServicoService {
    private final ServicoRepository servicoRepository;
    private final AgendaRepository agendaRepository;

    public ServicoService(ServicoRepository servicoRepository, AgendaRepository agendaRepository) {
        this.servicoRepository = servicoRepository;
        this.agendaRepository = agendaRepository;
    }

    public List<Servico> listar() {
        return servicoRepository.findAllByOrderByNomeAsc();
    }

    public Servico buscar(Integer id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));
    }

    @Transactional
    public Servico salvar(Servico servico) {
        if (servico.getValor() == null || servico.getValor() <= 0) {
            throw new BusinessException("valor", "O valor do serviço deve ser maior que zero.");
        }
        servico.setNome(servico.getNome().trim());
        return servicoRepository.save(servico);
    }

    @Transactional
    public void excluir(Integer id) {
        Servico servico = buscar(id);
        long qtd = agendaRepository.countByServicoId(id);
        if (qtd > 0) {
            throw new BusinessException("Não é possível excluir o serviço \"" + servico.getNome()
                    + "\": ele está vinculado a " + qtd + " agendamento(s).");
        }
        servicoRepository.delete(servico);
    }

    public Map<Integer, Long> agendamentosPorServico() {
        Map<Integer, Long> mapa = new HashMap<>();
        for (Servico s : servicoRepository.findAll()) {
            mapa.put(s.getId(), agendaRepository.countByServicoId(s.getId()));
        }
        return mapa;
    }
}
