CREATE TABLE IF NOT EXISTS sentence_category
(
    description TEXT primary key,
    category TEXT NOT NULL
);

INSERT INTO sentence_category VALUES ('Voluntary Supervision','UNKNOWN');
INSERT INTO sentence_category VALUES ('Sex Offender Sent Extended pre Oct 1998','UNKNOWN');
INSERT INTO sentence_category VALUES ('Pre/Post Release s105 C&D Act 1998','UNKNOWN');
INSERT INTO sentence_category VALUES ('Violent Offender Order','UNKNOWN');
INSERT INTO sentence_category VALUES ('Youth Rehabilitation Order','UNKNOWN');
INSERT INTO sentence_category VALUES ('ORA 14 Day Fixed Term Recall','RECALL');
INSERT INTO sentence_category VALUES ('ORA 14 Day Fixed Term Recall from HDC','RECALL');
INSERT INTO sentence_category VALUES ('Imprisoned in Default of a fine','DETERMINATE');
INSERT INTO sentence_category VALUES ('Imprisonment in Default of Fine','DETERMINATE');
INSERT INTO sentence_category VALUES ('Sentencing Code Standard Determinate Sentence','DETERMINATE');
INSERT INTO sentence_category VALUES ('CJA03 Standard Determinate Sentence','DETERMINATE');
INSERT INTO sentence_category VALUES ('ORA Sentencing Code Standard Determinate Sentence','DETERMINATE');
INSERT INTO sentence_category VALUES ('ORA CJA03 Standard Determinate Sentence','DETERMINATE');
INSERT INTO sentence_category VALUES ('Automatic Life','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Automatic Life Sec 273 Sentencing Code (18 - 20)','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Automatic Life Sec 283 Sentencing Code (21+)','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Automatic Life Sec 224A 03','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Adult prisoner 2003 CJA','UNKNOWN');
INSERT INTO sentence_category VALUES ('Adult Imprisonment less than 12 months','DETERMINATE');
INSERT INTO sentence_category VALUES ('ORA Breach Top Up Supervision','UNKNOWN');
INSERT INTO sentence_category VALUES ('Civil Imprisonment','DETERMINATE');
INSERT INTO sentence_category VALUES ('CIVIL IMPRISONMENT OVER 12 MONTHS SENTENCE','DETERMINATE');
INSERT INTO sentence_category VALUES ('Adult Imprison above 12 mths below 4 yrs','DETERMINATE');
INSERT INTO sentence_category VALUES ('Recalled from Curfew Conditions','RECALL');
INSERT INTO sentence_category VALUES ('Breach of Curfew','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('ORA Recalled from Curfew Conditions','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Detention For Life','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Adult Discretionary Life','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Detention For Public Protection','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Detention & training order notice of sup','UNKNOWN');
INSERT INTO sentence_category VALUES ('Detention and Training Order','DETERMINATE');
INSERT INTO sentence_category VALUES ('ORA Detention and Training Order','DETERMINATE');
INSERT INTO sentence_category VALUES ('EDS Sec 266 Sentencing Code (18 - 20)','DETERMINATE');
INSERT INTO sentence_category VALUES ('EDS Sec 279 Sentencing Code (21+)','DETERMINATE');
INSERT INTO sentence_category VALUES ('EDS Sec 254 Sentencing Code (U18)','DETERMINATE');
INSERT INTO sentence_category VALUES ('Extended Sent Public Protection CJA 03','DETERMINATE');
INSERT INTO sentence_category VALUES ('Sent Extended Sec 86 of PCC(S) Act 2000','DETERMINATE');
INSERT INTO sentence_category VALUES ('Fixed Term Recall Pre ORA Sentence','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Fixed Term Recall while on HDC','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('ORA Fixed Term Recall while on HDC','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('ORA 28 Day Fixed Term Recall','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('FTR Schedule 15 Offender','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('ORA FTR Schedule 15 Offender','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('FTR Sch 18 Sentencing Code Offender','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('ORA FTR Sch 18 Sentencing Code Offender','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('HDC (adults between 1 & 4 years)','UNKNOWN');
INSERT INTO sentence_category VALUES ('HDC PSS All Purpose AP Licence','UNKNOWN');
INSERT INTO sentence_category VALUES ('HDC PSS Notice of Supervision','UNKNOWN');
INSERT INTO sentence_category VALUES ('Recalled from HDC (not for curfew violation)','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Inability to Monitor','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('ORA HDC Recall (not curfew violation)','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Detention During Her Majesty'' s   Pleasure','UNKNOWN');
INSERT INTO sentence_category VALUES ('Detention During Her Majesty''s Pleasure','UNKNOWN');
INSERT INTO sentence_category VALUES ('HDC (adults under 12 months)','UNKNOWN');
INSERT INTO sentence_category VALUES ('HDC (all young offenders)','UNKNOWN');
INSERT INTO sentence_category VALUES ('Indeterminate Sentence for the Public Protection','INDETERMINATE');
INSERT INTO sentence_category VALUES ('EDS LASPO Automatic Release','DETERMINATE');
INSERT INTO sentence_category VALUES ('EDS LASPO Discretionary Release','DETERMINATE');
INSERT INTO sentence_category VALUES ('Legacy (pre 1991 Act)','UNKNOWN');
INSERT INTO sentence_category VALUES ('Life Imprisonment or Detention S.53(1) CYPA 1933','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Licence Recall','RECALL');
INSERT INTO sentence_category VALUES ('Recall from Automatic Life','INDETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Recall from Automatic Life Sec 273 Sentencing Code (18 - 20)','INDETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Recall from Automatic Life Sec 283 Sentencing Code (21+)','INDETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Recall from Automatic Life Sec 224A 03','INDETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Recall from Discretionary Life','INDETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Licence recall from DPP Sentence','INDETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('LR - EDS Sec 266 Sentencing Code (18 - 20)','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('LR - EDS Sec 279 Sentencing Code (21+)','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('LR - EDS Sec 254 Sentencing Code (U18)','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Licence recall from Extended Sentence for Public Protection','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Licence recall from Extended Sentence','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Licence recall from IPP Sentence','INDETERMINATE');
INSERT INTO sentence_category VALUES ('LR - EDS LASPO Automatic Release','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('LR - EDS LASPO Discretionary Release','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Recall to Custody Indeterminate Sentence','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Recall to Custody Mandatory Life','INDETERMINATE');
INSERT INTO sentence_category VALUES ('ORA Licence Recall','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('LR - Section 236A SOPC CJA03','DETERMINATE');
INSERT INTO sentence_category VALUES ('Recall Serious Off - 18 CJA03 POCCA 2000','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('LR - SOPC Sec 265 Sentencing Code (18 - 20)','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('LR - SOPC Sec 278 Sentencing Code (21+)','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Recall from YOI','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Recall Serious Offence Sec 250 Sentencing Code (U18)','DETERMINATE_RECALL');
INSERT INTO sentence_category VALUES ('Adult Mandatory Life','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Adult Imprison above 4 years (not Life)','DETERMINATE');
INSERT INTO sentence_category VALUES ('All Purpose PSS Licence','UNKNOWN');
INSERT INTO sentence_category VALUES ('Notice of Supervision','UNKNOWN');
INSERT INTO sentence_category VALUES ('Special sentence of detention for terrorist offenders of particular concern Sec 252A','DETERMINATE');
INSERT INTO sentence_category VALUES ('Section 236A SOPC CJA03','DETERMINATE');
INSERT INTO sentence_category VALUES ('Serious Offence Sec 250 Sentencing Code (U18)','DETERMINATE');
INSERT INTO sentence_category VALUES ('ORA Serious Offence Sec 250 Sentencing Code (U18)','DETERMINATE');
INSERT INTO sentence_category VALUES ('Custody For Life Sec 272 Sentencing Code (18 - 20)','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Custody For Life Sec 275 Sentencing Code (Murder) (U21)','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Serious Offence -18 POCCA 2000','DETERMINATE');
INSERT INTO sentence_category VALUES ('Serious Offence -18 CJA03 POCCA 2000','DETERMINATE');
INSERT INTO sentence_category VALUES ('ORA Serious Offence -18 CJA03 POCCA 2000','DETERMINATE');
INSERT INTO sentence_category VALUES ('Custody For Life - Under 21','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Custody For Life - Under 21 CJA03','INDETERMINATE');
INSERT INTO sentence_category VALUES ('Custody Life (18-21 Years Old)','INDETERMINATE');
INSERT INTO sentence_category VALUES ('SOPC Sec 265 Sentencing Code (18 - 20)','DETERMINATE');
INSERT INTO sentence_category VALUES ('SOPC Sec 278 Sentencing Code (21+)','DETERMINATE');
INSERT INTO sentence_category VALUES ('Serious Terrorism Sentence Sec 268A (18 - 20)','DETERMINATE');
INSERT INTO sentence_category VALUES ('Serious Terrorism Sentence Sec 282A (21+)','DETERMINATE');
INSERT INTO sentence_category VALUES ('Young Offender Notice of Sup, Post-CJA','UNKNOWN');
INSERT INTO sentence_category VALUES ('Young Offender Institution','DETERMINATE');
INSERT INTO sentence_category VALUES ('ORA Young Offender Institution','DETERMINATE');
INSERT INTO sentence_category VALUES ('Migrated Sentence Data','UNKNOWN');

