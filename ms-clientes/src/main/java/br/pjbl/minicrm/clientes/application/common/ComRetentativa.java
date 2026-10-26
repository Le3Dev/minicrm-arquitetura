package br.pjbl.minicrm.clientes.application.common;

import br.pjbl.minicrm.clientes.domain.ConcorrenciaException;

import java.util.function.Supplier;

/**
 * Repete a operação quando outra gravação venceu a corrida (concorrência otimista).
 * Ex.: duas instâncias da Function processando eventos do mesmo cliente ao mesmo tempo.
 */
public final class ComRetentativa {

    public static final int TENTATIVAS = 3;

    private ComRetentativa() {
    }

    public static <T> T executar(Supplier<T> operacao) {
        for (int tentativa = 1; ; tentativa++) {
            try {
                return operacao.get();
            } catch (ConcorrenciaException e) {
                if (tentativa >= TENTATIVAS) {
                    throw e;
                }
            }
        }
    }
}
