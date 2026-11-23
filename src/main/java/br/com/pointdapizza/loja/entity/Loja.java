package br.com.pointdapizza.loja.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loja")
public class Loja extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "nome", nullable = false, length = 120)
    public String nome;

    @Column(name = "slogan", nullable = false, length = 255)
    public String slogan;

    @Column(name = "logo_url", length = 500)
    public String logoUrl;

    @Column(name = "capa_url", length = 500)
    public String capaUrl;

    @Column(name = "aberta", nullable = false)
    public boolean aberta = true;

    @Column(name = "endereco", nullable = false, length = 300)
    public String endereco;

    @Column(name = "telefone", length = 20)
    public String telefone;

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm;

    @OneToMany(mappedBy = "loja", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dia ASC")
    public List<LojaHorario> horarios = new ArrayList<>();

    @OneToMany(mappedBy = "loja", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    public List<LojaFormaPagamento> formasPagamento = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void atualizarTimestamp() {
        this.atualizadoEm = OffsetDateTime.now();
    }
}
