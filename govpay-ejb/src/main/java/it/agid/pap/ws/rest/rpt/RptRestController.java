package it.agid.pap.ws.rest.rpt;

import it.gov.digitpa.schemas._2011.ws.paa.FaultBean;
import it.gov.digitpa.schemas._2011.ws.paa.NodoInviaRPTRisposta;
import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.model.Applicazione;
import it.govpay.bd.model.Portale;
import it.govpay.core.business.Pagamento;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.servizi.commons.EsitoOperazione;
import it.govpay.servizi.commons.TipoAutenticazione;
import it.govpay.servizi.commons.TipoVersamento;
import it.govpay.servizi.commons.Versamento;
import it.govpay.servizi.gpprt.GpAvviaTransazionePagamento;
import it.govpay.servizi.gpprt.GpAvviaTransazionePagamentoResponse;
import it.govpay.servizi.commons.Anagrafica;
import it.govpay.servizi.commons.Canale;
import it.govpay.web.rs.BaseRsService;
import it.agid.pap.model.RPT;
import it.agid.pap.util.FaultCodes;
import it.agid.pap.util.PapConstants;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;


@Path("/pap/{codDominio}/rpts")
public class RptRestController extends BaseRsService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response papRestInviaRpt(InputStream is, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("codDominio") String codDominio, RPT rpt) {

		logRequest(uriInfo, httpHeaders,"papRestInviaRpt", is);

		GpContext ctx = GpThreadLocal.get();

		BasicBD bd = null;
		try {
			ctx.log("pap.ricevutaRichiesta");
			bd = BasicBD.newInstance(ctx.getTransactionId());

			Portale portale = getPortaleAutenticato(bd);
			Applicazione applicazione = getApplicazioneAutenticata(bd);

			GpAvviaTransazionePagamento richiesta = new GpAvviaTransazionePagamento();
			richiesta.setAggiornaSeEsiste(false);
			richiesta.setAutenticazione(TipoAutenticazione.valueOf(rpt.getAutenticazioneSoggetto()));
			Canale canale = new Canale();
			canale.setCodCanale(rpt.getIdentificativoCanale());
			canale.setCodPsp(rpt.getIdentificativoPSP());
			canale.setTipoVersamento(TipoVersamento.fromValue(rpt.getDatiVersamento().getTipoVersamento()));
			richiesta.setCanale(canale);
			richiesta.setCodPortale(portale.getCodPortale());
			richiesta.setIbanAddebito(rpt.getDatiVersamento().getDatiSingoloVersamento().get(0).getIbanAccredito());
			richiesta.setUrlRitorno(null);

			if(rpt.getSoggettoVersante() != null) {
				Anagrafica versante = new Anagrafica();
				versante.setRagioneSociale(rpt.getSoggettoVersante().getAnagraficaVersante());
				versante.setCap(rpt.getSoggettoVersante().getCapVersante());
				versante.setCivico(rpt.getSoggettoVersante().getCivicoVersante());
				versante.setEmail(rpt.getSoggettoVersante().getEMailVersante());
				versante.setCodUnivoco(rpt.getSoggettoVersante().getIdentificativoUnivocoVersante().getCodiceIdentificativoUnivoco());
				versante.setIndirizzo(rpt.getSoggettoVersante().getIndirizzoVersante());
				versante.setLocalita(rpt.getSoggettoVersante().getLocalitaVersante());
				versante.setNazione(rpt.getSoggettoVersante().getNazioneVersante());
				versante.setProvincia(rpt.getSoggettoVersante().getProvinciaVersante());
				richiesta.setVersante(versante);
			}

			Versamento versamento = new Versamento();
			versamento.setAggiornabile(false);
			versamento.setAnnoTributario(null);
			versamento.setBundlekey(null);
			versamento.setCausale(null);
			versamento.setCodApplicazione(applicazione.getCodApplicazione());
			versamento.setCodDebito(null);
			versamento.setCodDominio(codDominio);
			versamento.setCodUnitaOperativa(rpt.getEnteBeneficiario().getCodiceUnitOperBeneficiario());
			versamento.setCodVersamentoEnte(rpt.getDatiVersamento().getIdentificativoUnivocoVersamento());
			versamento.setDataScadenza(null);

			Anagrafica debitore = new Anagrafica();
			debitore.setRagioneSociale(rpt.getSoggettoPagatore().getAnagraficaPagatore());
			debitore.setCap(rpt.getSoggettoPagatore().getCapPagatore());
			debitore.setCivico(rpt.getSoggettoPagatore().getCivicoPagatore());
			debitore.setEmail(rpt.getSoggettoPagatore().getEmailPagatore());
			debitore.setCodUnivoco(rpt.getSoggettoPagatore().getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco());
			debitore.setIndirizzo(rpt.getSoggettoPagatore().getIndirizzoPagatore());
			debitore.setLocalita(rpt.getSoggettoPagatore().getLocalitaPagatore());
			debitore.setNazione(rpt.getSoggettoPagatore().getNazionePagatore());
			debitore.setProvincia(rpt.getSoggettoPagatore().getProvinciaPagatore());
			versamento.setDebitore(debitore);

			versamento.setIuv(rpt.getDatiVersamento().getIdentificativoUnivocoVersamento());
			richiesta.getVersamentoOrVersamentoRef().add(versamento);

			it.govpay.bd.model.Canale.TipoVersamento tipoVersamento = it.govpay.bd.model.Canale.TipoVersamento.toEnum(canale.getTipoVersamento().toString());
			it.govpay.bd.model.Canale canaleModel = AnagraficaManager.getCanale(bd, canale.getCodPsp(), canale.getCodCanale(), tipoVersamento);

			Pagamento pagamento = new Pagamento(bd);
			GpAvviaTransazionePagamentoResponse avviaTransazione = pagamento.avviaTransazione(portale, richiesta, canaleModel);


			NodoInviaRPTRisposta wsResponse = new NodoInviaRPTRisposta();
			wsResponse.setEsito("OK");
			wsResponse.setRedirect(avviaTransazione.getUrlRedirect() != null ? 1 : 0);
			wsResponse.setUrl(avviaTransazione.getUrlRedirect());

			ctx.log("pap.ricevutaRichiestaOk");
			logResponse(uriInfo, httpHeaders,"papRestInviaRpt", toOutputStream(wsResponse));
			return Response.status(Status.CREATED).entity(wsResponse).build();
		} catch (GovPayException e) {
			e.log(log);
			ctx.log("pap.ricevutaRichiestaKo", e.getCodEsito().toString(), e.getMessage());
			FaultBean fault = new FaultBean();
			fault.setFaultCode(FaultCodes.PAP_UNEXPECTED_ERROR.name());
			fault.setFaultString(e.getMessage());
			logResponse(uriInfo, httpHeaders,"papRestInviaRpt", toOutputStream(fault));
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(fault).build();
		} catch (Exception e) {
			new GovPayException(e).log(log);
			ctx.log("gpapp.ricevutaRichiestaKo", EsitoOperazione.INTERNAL.toString(), e.getMessage());
			FaultBean fault = new FaultBean();
			fault.setFaultCode(FaultCodes.PAP_UNEXPECTED_ERROR.name());
			fault.setFaultString(e.getMessage());
			logResponse(uriInfo, httpHeaders,"papRestInviaRpt", toOutputStream(fault));
			return Response.status(Status.BAD_REQUEST).entity(fault).build();
		} finally {
			if(ctx != null) {
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
	}

	@POST
	@Path("/pagamentodiretto")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response papRestCaricaPagamentoDiretto(RPT rpt) {
		return Response.status(Status.BAD_REQUEST).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response papRestChiediStoricoRPT(
			@QueryParam("startDate") String startDate,
			@QueryParam("endDate") String endDate,
			@QueryParam("idUtente") String idUtente,
			@QueryParam("statoPagamento") String statoPagamento,
			@DefaultValue(PapConstants.LIST_RESULTS_PARAM) @QueryParam("risultati") Integer risultati,
			@DefaultValue(PapConstants.LIST_PAGE_PARAM) @QueryParam("pagina") Integer pagina) {

		return Response.status(Status.BAD_REQUEST)
				.entity("{\"esito\":\"" + PapConstants.ESITO_KO + "\"}")
				.build();
	}

	@PUT
	@Path("/pagamentodiretto")
	@Produces(MediaType.APPLICATION_JSON)
	public Response papRestAnnullaPagamentoDirettoDummy() {

		return Response.status(Status.BAD_REQUEST)
				.entity("{\"esito\":\"" + PapConstants.ESITO_KO + "\"}")
				.build();

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/pagamentodiretto/{iuv}")
	public Response papRestAnnullaPagamentoDiretto(@PathParam("iuv") String iuv) {

		return Response.status(Status.BAD_REQUEST)
				.entity("{\"esito\":\"" + PapConstants.ESITO_KO + "\"}")
				.build();
	}
}
