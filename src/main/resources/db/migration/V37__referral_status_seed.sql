INSERT INTO referral_status VALUES ('REFERRAL_STARTED','Referral Started','yellow', true );
INSERT INTO referral_status VALUES ('REFERRAL_SUBMITTED','Referral Submitted','light-pink', true );
INSERT INTO referral_status VALUES ('AWAITING_ASSESSMENT','Awaiting Assessment','orange', true );
INSERT INTO referral_status VALUES ('WITHDRAWN','Withdrawn','light-grey', true );
INSERT INTO referral_status VALUES ('ASSESSED_SUITABLE','Assessed As Suitable','light-blue', true );
INSERT INTO referral_status VALUES ('ON_PROGRAMME','On Programme','turquoise', true );
INSERT INTO referral_status VALUES ('SUITABLE_NOT_READY','Suitable But Not Ready','light-green', true );
INSERT INTO referral_status VALUES ('NOT_SUITABLE','Not Suitable','light-gray', true );
INSERT INTO referral_status VALUES ('DESELECTED','Deselected','light-grey', true );
INSERT INTO referral_status VALUES ('PROGRAMME_COMPLETE','Programme Complete','light-grey', true );

INSERT INTO referral_status VALUES ('ON_HOLD_AWAITING_ASSESSMENT','On Hold Awaiting Assessment','light-purple', true );

INSERT INTO referral_status_category VALUES('W_ADMIN', 'WITHDRAWN', 'Administrative error', true );
INSERT INTO referral_status_category VALUES('W_MOTIVATION', 'WITHDRAWN', 'Motivation and behaviour', true );
INSERT INTO referral_status_category VALUES('W_OPERATIONAL', 'WITHDRAWN', 'Operational', true );
INSERT INTO referral_status_category VALUES('W_PERSONAL', 'WITHDRAWN', 'Personal and health', true );
INSERT INTO referral_status_category VALUES('W_OTHER', 'WITHDRAWN', 'Other', true );

INSERT INTO referral_status_reason VALUES('W_DUPLICATE', 'W_ADMIN', 'Duplicate referral', true );
INSERT INTO referral_status_reason VALUES('W_ERROR', 'W_ADMIN', 'Submitted in error', true );

INSERT INTO referral_status_reason VALUES('W_BEHAVIOURAL', 'W_MOTIVATION', 'Behavioural problems', true );
INSERT INTO referral_status_reason VALUES('W_MOTIVATION', 'W_MOTIVATION', 'Motivation changed', true );

INSERT INTO referral_status_reason VALUES('W_NOT_AVAILABLE', 'W_OPERATIONAL', 'Course no longer available', true );
INSERT INTO referral_status_reason VALUES('W_TRANSFER_PROCESS', 'W_OPERATIONAL', 'In transfer process or transferred to another prison', true );
INSERT INTO referral_status_reason VALUES('W_NO_TIME', 'W_OPERATIONAL', 'Not enough time to complete programme', true );
INSERT INTO referral_status_reason VALUES('W_RELEASED', 'W_OPERATIONAL', 'Released after parole hearing', true );
INSERT INTO referral_status_reason VALUES('W_SENTENCE_ENDED', 'W_OPERATIONAL', 'Sentence ended or on probation', true );
INSERT INTO referral_status_reason VALUES('W_OTHER_RELEASE', 'W_OPERATIONAL', 'Other type of release', true );

INSERT INTO referral_status_reason VALUES('W_DEATH', 'W_PERSONAL', 'Death', true );
INSERT INTO referral_status_reason VALUES('W_INSTRUCTED_SOLICITOR', 'W_PERSONAL', 'Instructed by solicitor not to attend', true );
INSERT INTO referral_status_reason VALUES('W_HEALTH', 'W_PERSONAL', 'Medical or health reasons', true );
INSERT INTO referral_status_reason VALUES('W_NON_ASSOCIATIONS', 'W_PERSONAL', 'Non-associations', true );
INSERT INTO referral_status_reason VALUES('W_PERSONAL', 'W_PERSONAL', 'Personal reasons', true );
INSERT INTO referral_status_reason VALUES('W_SAFETY', 'W_PERSONAL', 'Personal safety', true );
INSERT INTO referral_status_reason VALUES('W_RISK_SCORES', 'W_PERSONAL', 'Risk scores changed', true );

INSERT INTO referral_status_category VALUES('D_MOTIVATION', 'DESELECTED', 'Motivation and behaviour', true );
INSERT INTO referral_status_category VALUES('D_OPERATIONAL', 'DESELECTED', 'Operational', true );
INSERT INTO referral_status_category VALUES('D_PERSONAL', 'DESELECTED', 'Personal and health', true );
INSERT INTO referral_status_category VALUES('D_OTHER', 'DESELECTED', 'Other', true );

INSERT INTO referral_status_reason VALUES('D_ATTITUDE', 'D_MOTIVATION', 'Attitude to group facilitators or others', true );
INSERT INTO referral_status_reason VALUES('D_BEHAVIOUR_IN', 'D_MOTIVATION', 'Behaviour in sessions', true );
INSERT INTO referral_status_reason VALUES('D_BEHAVIOUR_OUT', 'D_MOTIVATION', 'Behaviour outside sessions', true );
INSERT INTO referral_status_reason VALUES('D_ISSUES', 'D_MOTIVATION', 'Issues with other group members', true );
INSERT INTO referral_status_reason VALUES('D_MISSED', 'D_MOTIVATION', 'Missed too many sessions', true );
INSERT INTO referral_status_reason VALUES('D_MOTIVATION', 'D_MOTIVATION', 'Motivation changed', true );

INSERT INTO referral_status_reason VALUES('D_NOT_AVAILABLE', 'D_OPERATIONAL', 'Course no longer available', true );
INSERT INTO referral_status_reason VALUES('D_DEPORTED', 'D_OPERATIONAL', 'Deported', true );
INSERT INTO referral_status_reason VALUES('D_DID_NOT_RETURN', 'D_OPERATIONAL', 'Did not return from court', true );
INSERT INTO referral_status_reason VALUES('D_SEGREGATION', 'D_OPERATIONAL', 'In segregation unit', true );
INSERT INTO referral_status_reason VALUES('D_TRANSFER_PROCESS', 'D_OPERATIONAL', 'In transfer process or transferred to another prison', true );
INSERT INTO referral_status_reason VALUES('D_NOT_ALLOWED', 'D_OPERATIONAL', 'Not allowed out of cell (Rule 53)', true );
INSERT INTO referral_status_reason VALUES('D_REGIME', 'D_OPERATIONAL', 'Regime considerations', true );
INSERT INTO referral_status_reason VALUES('D_RELEASED_PAROLE', 'D_OPERATIONAL', 'Released after parole hearing', true );
INSERT INTO referral_status_reason VALUES('D_SAFER_CUSTODY', 'D_OPERATIONAL', 'Safer custody issues', true );
INSERT INTO referral_status_reason VALUES('D_SENTENCE_ENDED', 'D_OPERATIONAL', 'Sentence ended or on probation', true );
INSERT INTO referral_status_reason VALUES('D_OTHER_RELEASE', 'D_OPERATIONAL', 'Other type of release', true );

INSERT INTO referral_status_reason VALUES('D_ALCOHOL_OR_DRUGS', 'D_PERSONAL', 'Alcohol or drug use', true );
INSERT INTO referral_status_reason VALUES('D_DEATH', 'D_PERSONAL', 'Death', true );
INSERT INTO referral_status_reason VALUES('D_INSTRUCTED_SOLICITOR', 'D_PERSONAL', 'Instructed by solicitor not to attend', true );
INSERT INTO referral_status_reason VALUES('D_HEALTH', 'D_PERSONAL', 'Medical or health reasons', true );



