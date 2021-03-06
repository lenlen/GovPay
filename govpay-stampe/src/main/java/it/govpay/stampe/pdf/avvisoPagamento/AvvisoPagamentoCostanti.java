package it.govpay.stampe.pdf.avvisoPagamento;

public class AvvisoPagamentoCostanti {

	// chiavi contenuto statico
	public static final String SEZIONE_DOVUTO_KEY = "sezioneDovuto";
	public static final String SEZIONE_DISPONIBILITA_KEY = "sezioneDisponibilita";
	public static final String SEZIONE_COMUNICAZIONI_KEY = "sezioneComunicazioni";
	public static final String SEZIONE_PAGOPA_KEY = "sezionePagoPA";
	
	public static final String[] SEZIONE_STATICA_KEYS  = {
			SEZIONE_DOVUTO_KEY,	SEZIONE_DISPONIBILITA_KEY,SEZIONE_COMUNICAZIONI_KEY,SEZIONE_PAGOPA_KEY
	};
	
	public static final String ENTE_CREDITORE_KEY = "$ENTE_CREDITORE$";
	public static final String URL_ENTE_CREDITORE_KEY = "$URL_ENTE_CREDITORE$";
}
