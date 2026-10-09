package com.finisus.adapters.out.persistence;

import com.finisus.application.ports.out.DadosPessoaisPort;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DadosPessoaisJdbcAdapter implements DadosPessoaisPort {
	private final JdbcTemplate jdbc;
	public DadosPessoaisJdbcAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

	@Override
	@Transactional(readOnly = true)
	public Map<String, List<Map<String, Object>>> exportar(Long usuarioId) {
		Map<String, List<Map<String, Object>>> secoes = new LinkedHashMap<>();
		adicionar(secoes, "perfil", "select id,nome,email,ativo,criado_em,sessao_versao from usuario where id=?", usuarioId);
		adicionar(secoes, "bancos", "select id,nome,codigo,ativo from banco where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "contas", "select id,nome,tipo,banco_id,saldo,ativo,criado_em,version from conta where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "categorias", "select id,nome,categoria_pai_id,ativo from categoria where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "meiosPagamento", "select id,nome,ativo from meio_pagamento where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "itens", "select id,nome,categoria_padrao_id,ativo from item where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "transacoes", "select id,tipo,valor,data,descricao,conta_id,categoria_id,meio_pagamento_id,fatura_id,fatura_pagamento_id,compra_parcelada_id,recorrencia_id,transferencia_id,estornado_em,criado_em,version from transacao where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "itensTransacoes", "select i.id,i.transacao_id,i.item_id,i.descricao,i.quantidade,i.valor,i.categoria_id from transacao_item i join transacao t on t.id=i.transacao_id where t.usuario_id=? order by i.id", usuarioId);
		adicionar(secoes, "historicoTransacoes", "select h.id,h.transacao_id,h.campo_alterado,h.valor_anterior,h.valor_novo,h.alterado_por,h.alterado_em from transacao_historico h join transacao t on t.id=h.transacao_id where t.usuario_id=? order by h.id", usuarioId);
		adicionar(secoes, "recorrencias", "select id,nome,tipo,valor_esperado,dia_do_mes,categoria_id,conta_id,meio_pagamento_id,ativo from recorrencia where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "ocorrenciasRecorrencias", "select g.id,g.recorrencia_id,g.ano_mes,g.transacao_id from recorrencia_geracao g join recorrencia r on r.id=g.recorrencia_id where r.usuario_id=? order by g.id", usuarioId);
		adicionar(secoes, "previsoesMensais", "select id,ano_mes,categoria_id,valor_projetado_entrada,valor_projetado_saida,calculado_em from previsao_mensal where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "comprasParceladas", "select id,descricao,valor_total,numero_parcelas,data_compra,categoria_id,conta_id,cancelada_em from compra_parcelada where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "cartoes", "select id,nome,limite,dia_fechamento,dia_vencimento,ativo from cartao_credito where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "faturas", "select f.id,f.cartao_id,f.ano_mes,f.data_fechamento,f.data_vencimento,f.status,f.conta_pagamento_id,f.cancelada_em,f.version from fatura f join cartao_credito c on c.id=f.cartao_id where c.usuario_id=? order by f.id", usuarioId);
		adicionar(secoes, "pagamentosFaturas", "select id,fatura_id,transacao_id,conta_id,valor,credito,data_pagamento,estornado_em,version from pagamento_fatura where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "aplicacoesCreditoFatura", "select a.id,a.pagamento_origem_id,a.fatura_destino_id,a.valor,a.criada_em from aplicacao_credito_fatura a join pagamento_fatura p on p.id=a.pagamento_origem_id join fatura f on f.id=a.fatura_destino_id join cartao_credito c on c.id=f.cartao_id where p.usuario_id=? or c.usuario_id=? order by a.id", usuarioId, usuarioId);
		adicionar(secoes, "obrigacoes", "select id,descricao,credor,valor,valor_pago,data_vencimento,conta_pagamento_id,categoria_id,status,data_liquidacao,transacao_id,cancelada_em,version from obrigacao_financeira where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "pagamentosObrigacoes", "select id,obrigacao_id,transacao_id,valor,juros,encargos,desconto,data_pagamento,estornado_em,version from pagamento_obrigacao where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "financiamentos", "select id,descricao,principal,taxa_juros_mensal,numero_parcelas,data_inicio,conta_id,status,finalizado_em,cancelada_em,cronograma_versao,financiamento_origem_id from financiamento where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "parcelasFinanciamentos", "select p.id,p.financiamento_id,p.numero,p.data_vencimento,p.valor,p.valor_principal,p.juros,p.encargos,p.saldo_devedor_inicial,p.saldo_devedor_final,p.status,p.transacao_id,p.version from parcela_financiamento p join financiamento f on f.id=p.financiamento_id where f.usuario_id=? order by p.id", usuarioId);
		adicionar(secoes, "historicoParcelasFinanciamentos", "select h.id,h.financiamento_id,h.cronograma_versao,h.parcela_id_origem,h.numero,h.valor,h.valor_principal,h.juros,h.encargos,h.saldo_devedor_inicial,h.saldo_devedor_final,h.data_vencimento,h.status,h.transacao_id,h.registrado_em from parcela_financiamento_historico h join financiamento f on f.id=h.financiamento_id where f.usuario_id=? order by h.id", usuarioId);
		adicionar(secoes, "investimentos", "select id,nome,tipo,conta_origem_id,conta_custodia_id,ativo from investimento where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "movimentosInvestimentos", "select m.id,m.investimento_id,m.tipo,m.valor,m.data,m.transacao_id,m.movimento_origem_id,m.estornado_em from movimento_investimento m join investimento i on i.id=m.investimento_id where i.usuario_id=? order by m.id", usuarioId);
		adicionar(secoes, "posicoesInvestimentos", "select p.id,p.investimento_id,p.valor,p.data_referencia,p.version from posicao_investimento p join investimento i on i.id=p.investimento_id where i.usuario_id=? order by p.id", usuarioId);
		adicionar(secoes, "transferencias", "select id,conta_origem_id,conta_destino_id,valor,data_transferencia,descricao,status,estornada_em,criado_em,version from transferencia_conta where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "ajustesSaldo", "select id,conta_id,saldo_anterior,saldo_calculado_anterior,saldo_informado,valor_ajuste,motivo,data_ajuste,criado_em,version from ajuste_saldo_conta where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "importacoes", "select id,banco_id,nome_arquivo,leitor,tipo_documento,tipo_documento_pretendido,identificador_origem,periodo_inicio,periodo_fim,data_vencimento,saldo_inicial,saldo_final,valor_total,status,conta_id,fatura_id,criado_em,version from importacao_financeira where usuario_id=? order by id", usuarioId);
		adicionar(secoes, "lancamentosImportados", "select l.id,l.importacao_id,l.ordem,l.data,l.descricao,l.conteudo_original,l.data_original,l.descricao_original,l.valor_original,l.tipo_original,l.valor,l.tipo,l.pendente_confirmacao,l.motivo_pendencia,l.estado,l.importar,l.categoria_id,l.item_id,l.transacao_id,l.obrigacao_financeira_id from lancamento_importado l join importacao_financeira i on i.id=l.importacao_id where i.usuario_id=? order by l.id", usuarioId);
		adicionar(secoes, "revisoesLancamentosImportados", "select r.id,r.lancamento_importado_id,r.revisado_por,r.decisao,r.motivo_incerteza,r.justificativa,r.data_anterior,r.descricao_anterior,r.valor_anterior,r.tipo_anterior,r.importar_anterior,r.categoria_id_anterior,r.item_id_anterior,r.transacao_id_anterior,r.obrigacao_id_anterior,r.data_nova,r.descricao_nova,r.valor_novo,r.tipo_novo,r.importar_novo,r.categoria_id_nova,r.item_id_novo,r.transacao_id_nova,r.obrigacao_id_nova,r.revisado_em from revisao_lancamento_importado r join lancamento_importado l on l.id=r.lancamento_importado_id join importacao_financeira i on i.id=l.importacao_id where i.usuario_id=? or r.revisado_por=? order by r.id", usuarioId, usuarioId);
		adicionar(secoes, "configuracaoCompartilhamento", "select id,aceita_compartilhamento from configuracao_compartilhamento where usuario_id=?", usuarioId);
		adicionar(secoes, "divisoesCompartilhadas", "select distinct d.id,d.criador_id,d.nome,d.status,d.version from divisao_compartilhada d left join participante_divisao_compartilhada p on p.divisao_compartilhada_id=d.id where d.criador_id=? or p.usuario_id=? order by d.id", usuarioId, usuarioId);
		adicionar(secoes, "participacoesDivisoes", "select p.id,p.divisao_compartilhada_id,p.usuario_id,p.percentual from participante_divisao_compartilhada p where p.usuario_id=? or p.divisao_compartilhada_id in (select d.id from divisao_compartilhada d where d.criador_id=?) order by p.id", usuarioId, usuarioId);
		adicionar(secoes, "historicoParticipacoesDivisoes", "select h.id,h.divisao_compartilhada_id,h.usuario_id,h.percentual,h.vigente_desde,h.vigente_ate from historico_participante_divisao h where h.usuario_id=? or h.divisao_compartilhada_id in (select d.id from divisao_compartilhada d where d.criador_id=?) order by h.id", usuarioId, usuarioId);
		adicionar(secoes, "vinculosTransacoesDivisoes", "select v.id,v.divisao_compartilhada_id,v.transacao_id,v.status_snapshot,v.base_compartilhada,v.cancelado_em,v.cancelado_por,v.transacao_ativa_id from transacao_divisao_compartilhada v join divisao_compartilhada d on d.id=v.divisao_compartilhada_id left join participante_divisao_compartilhada p on p.divisao_compartilhada_id=d.id where d.criador_id=? or p.usuario_id=? group by v.id,v.divisao_compartilhada_id,v.transacao_id,v.status_snapshot,v.base_compartilhada,v.cancelado_em,v.cancelado_por,v.transacao_ativa_id order by v.id", usuarioId, usuarioId);
		adicionar(secoes, "responsabilidadesDivisoes", "select r.id,r.vinculo_id,r.usuario_id,r.percentual,r.valor_devido from responsabilidade_transacao_divisao r join transacao_divisao_compartilhada v on v.id=r.vinculo_id join divisao_compartilhada d on d.id=v.divisao_compartilhada_id where r.usuario_id=? or d.criador_id=? order by r.id", usuarioId, usuarioId);
		adicionar(secoes, "alocacoesPagamentosDivisoes", "select a.id,a.vinculo_id,a.transacao_id,a.pagador_id,a.valor,a.criada_em,a.cancelada_em,a.cancelada_por from alocacao_pagamento_divisao a join transacao_divisao_compartilhada v on v.id=a.vinculo_id join divisao_compartilhada d on d.id=v.divisao_compartilhada_id where a.pagador_id=? or d.criador_id=? order by a.id", usuarioId, usuarioId);
		adicionar(secoes, "reembolsosDivisoes", "select r.id,r.divisao_id,r.transacao_id,r.pagador_id,r.recebedor_id,r.valor,r.criado_em,r.cancelado_em,r.cancelado_por,r.transacao_ativa_id from reembolso_divisao r join divisao_compartilhada d on d.id=r.divisao_id where r.pagador_id=? or r.recebedor_id=? or d.criador_id=? order by r.id", usuarioId, usuarioId, usuarioId);
		adicionar(secoes, "solicitacoesPrivacidade", "select id,tipo,status,motivo,solicitada_em,concluida_em,observacao,version from solicitacao_privacidade where usuario_id=? order by id", usuarioId);
		return Map.copyOf(secoes);
	}

	private void adicionar(Map<String, List<Map<String, Object>>> secoes, String nome, String sql, Object... parametros) {
		secoes.put(nome, jdbc.queryForList(sql, parametros));
	}
}
