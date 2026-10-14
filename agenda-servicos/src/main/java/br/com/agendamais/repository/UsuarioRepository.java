package br.com.agendamais.repository;

import br.com.agendamais.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsuarioIgnoreCase(String usuario);

    List<Usuario> findAllByOrderByUsuarioAsc();

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByUsuarioIgnoreCaseAndIdNot(String usuario, Integer id);

    boolean existsByTipoAcessoId(Integer tipoAcessoId);

    long countByTipoAcessoId(Integer tipoAcessoId);
}
