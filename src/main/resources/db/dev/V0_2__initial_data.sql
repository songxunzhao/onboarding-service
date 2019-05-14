INSERT INTO oauth_client_details(
            client_id, resource_ids, client_secret, scope, authorized_grant_types,
            web_server_redirect_uri, authorities, access_token_validity,
            refresh_token_validity, additional_information, autoapprove)

    SELECT 'onboarding-client', 'onboarding-service', 'onboarding-client', 'onboarding', 'mobile_id,id_card,refresh_token',
            null, null, 1800,
            1800, null, null
    WHERE
      NOT EXISTS (
        SELECT * FROM oauth_client_details WHERE client_id = 'onboarding-client'
      );

INSERT INTO oauth_client_details(
  client_id, resource_ids, client_secret, scope, authorized_grant_types,
  web_server_redirect_uri, authorities, access_token_validity,
  refresh_token_validity, additional_information, autoapprove)

  SELECT 'epis-service', null, 'epis-service', 'user', null,
    null, 'ROLE_TRUSTED_CLIENT', null,
    null, null, null
  WHERE
    NOT EXISTS (
        SELECT * FROM oauth_client_details WHERE client_id = 'epis-service'
    );

INSERT INTO fund_manager(
            id, name)
    SELECT 1, 'Tuleva'
    WHERE
      NOT EXISTS (
        SELECT * FROM fund_manager WHERE name = 'Tuleva'
      );

INSERT INTO fund_manager(
            id, name)
    SELECT 2, 'LHV'
    WHERE
      NOT EXISTS (
        SELECT * FROM fund_manager WHERE name = 'LHV'
      );

INSERT INTO fund_manager(
            id, name)
    SELECT 3, 'Swedbank'
    WHERE
      NOT EXISTS (
        SELECT * FROM fund_manager WHERE name = 'Swedbank'
      );
INSERT INTO fund_manager(
            id, name)
    SELECT 4, 'Luminor'
    WHERE
      NOT EXISTS (
        SELECT * FROM fund_manager WHERE name = 'Luminor'
      );

INSERT INTO fund_manager(
            id, name)
    SELECT 5, 'SEB'
    WHERE
      NOT EXISTS (
        SELECT * FROM fund_manager WHERE name = 'SEB'
      );

INSERT INTO fund(
            isin, name, management_fee_rate, fund_manager_id)
    SELECT 'EE3600109435', 'Tuleva Maailma Aktsiate Pensionifond', 0.0034, 1
    WHERE
      NOT EXISTS (
        SELECT * FROM fund WHERE id = 1
      );

INSERT INTO fund(
            isin, name, management_fee_rate, fund_manager_id)
    SELECT 'EE3600109443', 'Tuleva Maailma Võlakirjade Pensionifond', 0.0034, 1
    WHERE
      NOT EXISTS (
        SELECT * FROM fund WHERE id = 2
      );

INSERT INTO fund(
            isin, name, management_fee_rate, fund_manager_id)
    SELECT 'AE123232337', 'LHV XL', 0.0095, 2
    WHERE
      NOT EXISTS (
        SELECT * FROM fund WHERE id = 3
      );

INSERT INTO fund(
            isin, name, management_fee_rate, fund_manager_id)
    SELECT 'EE3600019790', 'Kohustuslik Pensionifond Danske Pension 25', 0.96425, 2
    WHERE
      NOT EXISTS (
        SELECT * FROM fund WHERE id = 4
      );

INSERT INTO fund(
            isin, name, management_fee_rate, fund_manager_id)
    SELECT 'EE3600019774', 'LHV Pensionifond M', 0.06400, 2
    WHERE
      NOT EXISTS (
        SELECT * FROM fund WHERE id = 5
      );

INSERT INTO fund(
  isin, name, management_fee_rate, fund_manager_id)
  SELECT 'EE3600109401', 'LHV Pensionifond Indeks', 0.03900, 2
  WHERE
    NOT EXISTS (
        SELECT * FROM fund WHERE id = 6
    );

INSERT INTO fund(
  isin, name, management_fee_rate, fund_manager_id)
  SELECT 'EE3600019758', 'Swedbank Pensionifond K3 (kasvustrateegia)', 0.09200, 2
  WHERE
    NOT EXISTS (
        SELECT * FROM fund WHERE id = 7
    );


INSERT INTO users(
            active, personal_code, first_name, last_name, created_date, updated_date, member_number, phone_number, email)
    SELECT true, '38812022762', 'Jordan', 'Valdma', '2015-01-31 14:06:01', '2017-01-31 14:06:01', 100, '5523533', 'jordan@mail.ee'
    WHERE
      NOT EXISTS (
        SELECT * FROM users WHERE id = 1
      );

INSERT INTO users(
            active, personal_code, first_name, last_name, created_date, updated_date, member_number, phone_number, email)
    SELECT true, '39911223344', 'Firstname', 'Lastname', '2015-01-31 14:06:01', '2017-01-31 14:06:01', 1, '1234567', 'first.last@mail.ee'
    WHERE
      NOT EXISTS (
        SELECT * FROM users WHERE id = 2
      );

INSERT INTO users(
            active, personal_code, first_name, last_name, created_date, updated_date, member_number, phone_number, email)
    SELECT true, '37807256017', 'Ziim', 'Kaba', '2015-01-31 14:06:01', '2017-01-31 14:06:01', 2419, '1234567', 'ziim@mail.ee'
    WHERE
      NOT EXISTS (
        SELECT * FROM users WHERE id = 3
      );

INSERT INTO initial_capital(
            id, user_id, amount, currency)
    SELECT 1, 1, 10000.00, 'EUR'
    WHERE
      NOT EXISTS (
        SELECT * FROM initial_capital WHERE id = 1
      );

-- run this for default inline signup access token
-- INSERT INTO public.oauth_access_token (token_id, token, authentication_id, user_name, client_id, authentication, refresh_token) VALUES ('a3a3d2631ffd1f20f4b4ad75bf55bc10', E'\\xACED0005737200436F72672E737072696E676672616D65776F726B2E73656375726974792E6F61757468322E636F6D6D6F6E2E44656661756C744F4175746832416363657373546F6B656E0CB29E361B24FACE0200064C00156164646974696F6E616C496E666F726D6174696F6E74000F4C6A6176612F7574696C2F4D61703B4C000A65787069726174696F6E7400104C6A6176612F7574696C2F446174653B4C000C72656672657368546F6B656E74003F4C6F72672F737072696E676672616D65776F726B2F73656375726974792F6F61757468322F636F6D6D6F6E2F4F417574683252656672657368546F6B656E3B4C000573636F706574000F4C6A6176612F7574696C2F5365743B4C0009746F6B656E547970657400124C6A6176612F6C616E672F537472696E673B4C000576616C756571007E000578707372001E6A6176612E7574696C2E436F6C6C656374696F6E7324456D7074794D6170593614855ADCE7D002000078707372000E6A6176612E7574696C2E44617465686A81014B59741903000078707708000001745F71F2977870737200256A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F6469666961626C65536574801D92D18F9B80550200007872002C6A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F6469666961626C65436F6C6C656374696F6E19420080CB5EF71E0200014C0001637400164C6A6176612F7574696C2F436F6C6C656374696F6E3B7870737200176A6176612E7574696C2E4C696E6B656448617368536574D86CD75A95DD2A1E020000787200116A6176612E7574696C2E48617368536574BA44859596B8B7340300007870770C000000103F4000000000000274000C6372656174655F757365727374000566756E64737874000662656172657274002436623333386261322D383035632D343330302D393334312D623338626234616433346139', '89aa7925d3ae5396258c8944b610863f', null, 'tuleva.ee', E'\\xACED0005737200416F72672E737072696E676672616D65776F726B2E73656375726974792E6F61757468322E70726F76696465722E4F417574683241757468656E7469636174696F6EBD400B02166252130200024C000D73746F7265645265717565737474003C4C6F72672F737072696E676672616D65776F726B2F73656375726974792F6F61757468322F70726F76696465722F4F4175746832526571756573743B4C00127573657241757468656E7469636174696F6E7400324C6F72672F737072696E676672616D65776F726B2F73656375726974792F636F72652F41757468656E7469636174696F6E3B787200476F72672E737072696E676672616D65776F726B2E73656375726974792E61757468656E7469636174696F6E2E416273747261637441757468656E7469636174696F6E546F6B656ED3AA287E6E47640E0200035A000D61757468656E746963617465644C000B617574686F7269746965737400164C6A6176612F7574696C2F436F6C6C656374696F6E3B4C000764657461696C737400124C6A6176612F6C616E672F4F626A6563743B787000737200266A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F6469666961626C654C697374FC0F2531B5EC8E100200014C00046C6973747400104C6A6176612F7574696C2F4C6973743B7872002C6A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F6469666961626C65436F6C6C656374696F6E19420080CB5EF71E0200014C00016371007E00047870737200136A6176612E7574696C2E41727261794C6973747881D21D99C7619D03000149000473697A65787000000001770400000001737200426F72672E737072696E676672616D65776F726B2E73656375726974792E636F72652E617574686F726974792E53696D706C654772616E746564417574686F7269747900000000000001A40200014C0004726F6C657400124C6A6176612F6C616E672F537472696E673B787074000B524F4C455F434C49454E547871007E000C707372003A6F72672E737072696E676672616D65776F726B2E73656375726974792E6F61757468322E70726F76696465722E4F41757468325265717565737400000000000000010200075A0008617070726F7665644C000B617574686F72697469657371007E00044C000A657874656E73696F6E7374000F4C6A6176612F7574696C2F4D61703B4C000B726564697265637455726971007E000E4C00077265667265736874003B4C6F72672F737072696E676672616D65776F726B2F73656375726974792F6F61757468322F70726F76696465722F546F6B656E526571756573743B4C000B7265736F7572636549647374000F4C6A6176612F7574696C2F5365743B4C000D726573706F6E7365547970657371007E0014787200386F72672E737072696E676672616D65776F726B2E73656375726974792E6F61757468322E70726F76696465722E426173655265717565737436287A3EA37169BD0200034C0008636C69656E74496471007E000E4C001172657175657374506172616D657465727371007E00124C000573636F706571007E0014787074000974756C6576612E6565737200256A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F6469666961626C654D6170F1A5A8FE74F507420200014C00016D71007E00127870737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F400000000000037708000000040000000274000A6772616E745F74797065740012636C69656E745F63726564656E7469616C73740009636C69656E745F696474000974756C6576612E656578737200256A6176612E7574696C2E436F6C6C656374696F6E7324556E6D6F6469666961626C65536574801D92D18F9B80550200007871007E0009737200176A6176612E7574696C2E4C696E6B656448617368536574D86CD75A95DD2A1E020000787200116A6176612E7574696C2E48617368536574BA44859596B8B7340300007870770C000000103F4000000000000274000C6372656174655F757365727374000566756E647378017371007E0023770C000000103F4000000000000171007E000F787371007E001A3F40000000000000770800000010000000007870707371007E0023770C000000103F40000000000000787371007E0023770C000000103F400000000000007870', null);
