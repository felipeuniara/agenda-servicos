package br.com.agendamais.service;

import br.com.agendamais.entity.TipoAcesso;
import br.com.agendamais.repository.TipoAcessoRepository;
import br.com.agendamais.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TipoAcessoService {
    private final TipoAcessoRepository tipoAcessoRepository;
    private final UsuarioRepository usuarioRepository;

    public TipoAcessoService(TipoAcessoRepository tipoAcessoRepository, UsuarioRepository usuarioRepository) {
        this.tipoAcessoRepository = tipoAcessoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<TipoAcesso> listar() {
        return tipoAcessoRepository.findAllByOrderByDescricaoAsc();
    }

    public TipoAcesso buscar(Integer id) {
        return tipoAcessoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de acesso não encontrado."));
    }

    @Transactional
    public TipoAcesso salvar(TipoAcesso dados) {
        String descricao = dados.getDescricao().trim();
        boolean duplicado = dados.getId() == null
                ? tipoAcessoRepository.existsByDescricaoIgnoreCase(descricao)
                : tipoAcessoRepository.existsByDescricaoIgnoreCaseAndIdNot(descricao, dados.getId());
        if (duplicado) {
            throw new BusinessException("descricao", "Já existe um tipo de acesso com esta descrição.");
        }
        if (dados.getId() != null) {
            TipoAcesso atual = buscar(dados.getId());
            atual.setDescricao(descricao);
            return tipoAcessoRepository.save(atual);
        }
        dados.setDescricao(descricao);
        return tipoAcessoRepository.save(dados);
    }

    @Transactional
    public void excluir(Integer id) {
        TipoAcesso tipo = buscar(id);
        long qtd = usuarioRepository.countByTipoAcessoId(id);
        if (qtd > 0) {
            throw new BusinessException("Não é possível excluir \"" + tipo.getDescricao()
                    + "\": há " + qtd + " usuário(s) com este tipo de acesso.");
        }
        tipoAcessoRepository.delete(tipo);
    }

    public Map<Integer, Long> usuariosPorTipo() {
        Map<Integer, Long> mapa = new HashMap<>();
        for (TipoAcesso t : tipoAcessoRepository.findAll()) {
            mapa.put(t.getId(), usuarioRepository.countByTipoAcessoId(t.getId()));
        }
        return mapa;
    }
}
