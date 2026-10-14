package br.com.agendamais.service;

import br.com.agendamais.entity.Cliente;
import br.com.agendamais.entity.Endereco;
import br.com.agendamais.repository.ClienteRepository;
import br.com.agendamais.repository.EnderecoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EnderecoService {
    private final EnderecoRepository enderecoRepository;
    private final ClienteRepository clienteRepository;

    public EnderecoService(EnderecoRepository enderecoRepository, ClienteRepository clienteRepository) {
        this.enderecoRepository = enderecoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<Endereco> listar() {
        return enderecoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Endereco buscar(Integer id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Endereço não encontrado."));
    }

    public Map<Integer, Cliente> clientesPorEndereco() {
        Map<Integer, Cliente> mapa = new HashMap<>();
        for (Cliente c : clienteRepository.findAll()) {
            if (c.getEndereco() != null) {
                mapa.put(c.getEndereco().getId(), c);
            }
        }
        return mapa;
    }

    public Optional<Cliente> clienteDoEndereco(Integer enderecoId) {
        return clienteRepository.findByEnderecoId(enderecoId);
    }

    public List<Cliente> clientesSemEndereco() {
        return clienteRepository.findByEnderecoIsNullOrderByNomeAsc();
    }

    @Transactional
    public Endereco salvar(Endereco dados, Integer clienteId) {
        normalizar(dados);
        if (dados.getLogradouro() == null) {
            throw new BusinessException("logradouro", "Informe o logradouro.");
        }
        if (dados.getCidade() == null) {
            throw new BusinessException("cidade", "Informe a cidade.");
        }
        if (dados.getUf() == null) {
            throw new BusinessException("uf", "Selecione a UF.");
        }

        if (dados.getId() != null) {
            Endereco atual = buscar(dados.getId());
            copiar(dados, atual);
            return enderecoRepository.save(atual);
        }

        Cliente cliente = null;
        if (clienteId != null) {
            cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new BusinessException("Cliente não encontrado."));
            if (cliente.getEndereco() != null) {
                throw new BusinessException("O cliente " + cliente.getNome() + " já possui um endereço cadastrado.");
            }
        }
        Endereco salvo = enderecoRepository.save(dados);
        if (cliente != null) {
            cliente.setEndereco(salvo);
            clienteRepository.save(cliente);
        }
        return salvo;
    }

    @Transactional
    public void excluir(Integer id) {
        Endereco endereco = buscar(id);
        Optional<Cliente> dono = clienteRepository.findByEnderecoId(id);
        if (dono.isPresent()) {
            Cliente cliente = dono.get();
            cliente.setEndereco(null);
            clienteRepository.save(cliente);
        } else {
            enderecoRepository.delete(endereco);
        }
    }

    public void normalizar(Endereco e, String prefixoCampo) {
        e.setLogradouro(limpar(e.getLogradouro()));
        e.setCidade(limpar(e.getCidade()));
        e.setComplemento(limpar(e.getComplemento()));
        e.setEstado(limpar(e.getEstado()));
        e.setCep(limpar(e.getCep()));
        e.setUf(limpar(e.getUf()));

        if (e.getUf() != null) {
            String uf = e.getUf().toUpperCase();
            if (!Ufs.valida(uf)) {
                throw new BusinessException(prefixoCampo + "uf", "UF inválida.");
            }
            e.setUf(uf);
            e.setEstado(Ufs.estado(uf));
        }
        if (e.getCep() != null) {
            String digitos = e.getCep().replaceAll("\\D", "");
            if (digitos.length() != 8) {
                throw new BusinessException(prefixoCampo + "cep", "CEP inválido. Use o formato 00000-000.");
            }
            e.setCep(digitos.substring(0, 5) + "-" + digitos.substring(5));
        }
        if (e.getNumero() != null && e.getNumero() <= 0) {
            throw new BusinessException(prefixoCampo + "numero", "O número deve ser maior que zero.");
        }
    }

    public void normalizar(Endereco e) {
        normalizar(e, "");
    }

    static void copiar(Endereco origem, Endereco destino) {
        destino.setLogradouro(origem.getLogradouro());
        destino.setNumero(origem.getNumero());
        destino.setComplemento(origem.getComplemento());
        destino.setCep(origem.getCep());
        destino.setCidade(origem.getCidade());
        destino.setUf(origem.getUf());
        destino.setEstado(origem.getEstado());
    }

    private static String limpar(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
