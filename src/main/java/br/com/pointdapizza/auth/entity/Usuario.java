package br.com.pointdapizza.auth.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    public UUID id;

    @Column(name = "nome", nullable = false, length = 150)
    public String nome;

    @Column(name = "telefone", length = 20)
    public String telefone;

    @Column(name = "telefone_whatsapp", nullable = false)
    public boolean telefoneWhatsapp = true;

    @Column(name = "email", length = 180)
    public String email;

    @Column(name = "endereco", length = 300)
    public String endereco;

    @Column(name = "complemento", length = 150)
    public String complemento;

    @Column(name = "senha_hash", nullable = false, length = 255)
    public String senhaHash;

    @Column(name = "foto_url", length = 500)
    public String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    public RoleUsuario role = RoleUsuario.USER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cadastrado_por_admin_id")
    public Usuario cadastradoPorAdmin;

    @Column(name = "ativo", nullable = false)
    public boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm;

    @PrePersist
    void prePersist() {
        OffsetDateTime agora = OffsetDateTime.now();
        if (this.role == null) {
            this.role = RoleUsuario.USER;
        }
        if (this.criadoEm == null) {
            this.criadoEm = agora;
        }
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }
}
