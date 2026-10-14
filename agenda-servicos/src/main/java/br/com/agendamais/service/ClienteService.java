package br.com.agendamais.service;

import br.com.agendamais.entity.Cliente;
import br.com.agendamais.entity.Endereco;
import br.com.agendamais.repository.AgendaRepository;
import br.com.agendamais.repository.ClienteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ClienteService {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private final ClienteRepository clienteRepository;
    private final AgendaRepository agendaRepository;
    private final EnderecoService enderecoService;

    public ClienteService(ClienteRepository clienteRepository, AgendaRepository agendaRepository,
                          EnderecoService enderecoService) {
        this.clienteRepository = clienteRepository;
        this.agendaRepository = agendaRepository;
        this.enderecoService = enderecoService;
    }

    public List<Cliente> listarPorNome() {
        return clienteRepository.findAllByOrderByNomeAsc();
    }

    public List<Cliente> pesquisar(String termo, String filtro, String ordem) {
        Sort sort = "nome".equals(ordem)
                ? Sort.by(Sort.Direction.ASC, "nome")
                : Sort.by(Sort.Direction.DESC, "id");
        List<Cliente> lista = clienteRepository.buscar(termo == null ? "" : termo.trim(), sort);
        if ("completo".equals(filtro)) {
            return lista.stream().filter(c -> c.getEndereco() != null && c.getEndereco().isCompleto()).toList();
        }
        if ("incompleto".equals(filtro)) {
            return lista.stream().filter(c -> c.getEndereco() == null || !c.getEndereco().isCompleto()).toList();
        }
        return lista;
    }

    public Cliente buscar(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado."));
    }

    @Transactional
    public Cliente salvar(Cliente dados) {
        String email = dados.getEmail() == null ? "" : dados.getEmail().trim().toLowerCase();

        if (!EMAIL.matcher(email).matches()) {
            throw new BusinessException("email", "Informe um e-mail válido (ex.: nome@dominio.com).");
        }
        boolean duplicado = dados.getId() == null
                ? clienteRepository.existsByEmailIgnoreCase(email)
                : clienteRepository.existsByEmailIgnoreCaseAndIdNot(email, dados.getId());
        if (duplicado) {
            throw new BusinessException("email", "Já existe um cliente cadastrado com este e-mail.");
        }

        Endereco enderecoInformado = dados.getEndereco();
        if (enderecoInformado != null) {
            enderecoService.normalizar(enderecoInformado, "endereco.");
            if (enderecoInformado.isVazio()) {
                enderecoInformado = null;
            }
        }

        Cliente cliente = dados.getId() == null ? new Cliente() : buscar(dados.getId());
        cliente.setNome(dados.getNome().trim());
        cliente.setTelefone(dados.getTelefone().trim());
        cliente.setEmail(email);

        if (enderecoInformado == null) {
            cliente.setEndereco(null);
        } else {
            Endereco destino = cliente.getEndereco() != null ? cliente.getEndereco() : new Endereco();
            EnderecoService.copiar(enderecoInformado, destino);
            cliente.setEndereco(destino);
        }
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void excluir(Integer id) {
        Cliente cliente = buscar(id);
        long qtd = agendaRepository.countByClienteId(id);
        if (qtd > 0) {
            throw new BusinessException("Não é possível excluir " + cliente.getNome()
                    + ": o cliente possui " + qtd + " agendamento(s) cadastrado(s).");
        }
        clienteRepository.delete(cliente);
    }

    public long quantidadeAgendamentos(Integer clienteId) {
        return agendaRepository.countByClienteId(clienteId);
    }
}
