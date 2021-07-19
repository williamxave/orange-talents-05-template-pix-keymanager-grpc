package br.com.zup.william.registra

import br.com.zup.william.registrabcb.TipoDaContaBCB

enum class TipoDeConta {
    CONTA_CORRENTE{
        override fun paraContaBCB(conta: TipoDeConta): TipoDaContaBCB {
            return TipoDaContaBCB.CACC
        }
                  },
    CONTA_POUPANCA {
        override fun paraContaBCB(conta: TipoDeConta): TipoDaContaBCB {
            return TipoDaContaBCB.SVGS
        }
    };

   abstract fun paraContaBCB(conta: TipoDeConta):TipoDaContaBCB
}
