package br.com.pointdapizza.cardapio.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria")
public class Categoria extends PanacheEntityBase {

    @Id
    @Column(name = "id", length = 64)
    public String id;

    @Column(name = "nome", nullable = false, length = 100)
    public String nome;

    @Column(name = "ordem", nullable = false)
    public Integer ordem;

    @Column(name = "imagem_url", length = 500)
    public String imagemUrl;

    @Column(name = "ativa", nullable = false)
    public boolean ativa = true;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm;

    @OneToMany(mappedBy = "categoria")
    @OrderBy("ordemExibicao ASC, nome ASC")
    public List<Produto> produtos = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
