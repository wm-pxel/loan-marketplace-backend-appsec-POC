------------------------------------
-- Create West Monroe Institution --
------------------------------------
INSERT INTO INSTITUTION_INFO
     ( INSTITUTION_UUID, INSTITUTION_NAME, OWNER_NAME, PERMISSION_SET_DESC, ACTIVE_IND )
       VALUES
     ( '1cb7d8dd-06bb-48fb-bf91-bd1d3db322ba', 'West Monroe Credit Association', 'Drew Dado', 'Lead and Participant', 'Y' );

------------------------------------
--    Create West Monroe Users    --
------------------------------------
INSERT INTO USER_INFO
     ( USER_UUID, INSTITUTION_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDR, PASSWORD_DESC, ACTIVE_IND )
       VALUES
     ( 'd1091f7d-b94f-4a45-afc7-1d33580e8a0c', 10, 'Joseph', 'Clevenger', 'jclevenger@westmonroe.com', '1234567890', 'Y' ),
     ( '23781ab1-e022-4981-8741-811b9e9b30d8', 10, 'Angel', 'Araya Quiros', 'aarayaquiros+lmdev@westmonroe.com', '1234567890', 'Y' ),
     ( 'b78738c6-3179-4a83-8e35-3f76197566bf', 10, 'Devin', 'Bezdicek', 'dbezdicek@westmonroe.com', '1234567890', 'Y' ),
     ( '964fb580-bd37-426d-9d82-e365be704c47', 10, 'Kristen', 'Rascher', 'krascher@westmonroe.com', '1234567890', 'Y' ),
     ( '25b94866-53d1-4f9d-8201-29bd3d42b149', 10, 'Ariane', 'Fund', 'afund@westmonroe.com', '1234567890', 'Y' ),
     ( '5bbae937-f274-4c5c-805e-86a5a171d8a0', 10, 'Lila', 'Werner', 'lwerner@westmonroe.com', '1234567890', 'Y' ),
     ( '267d168f-ffa1-428d-ab85-494f54421976', 10, 'Melina', 'Hernandez Leal', 'mhernandezleal@westmonroe.com', '1234567890', 'Y' ),
     ( '9aef074a-4fa9-4174-ac5d-d08d72242d8d', 10, 'Cole', 'Mitchell', 'cmitchell@westmonroe.com', '1234567890', 'Y' ),
     ( 'b8da2202-3ff4-440f-8814-d820a8c99ccc', 10, 'Letteer', 'Lewis', 'llewis@westmonroe.com', '1234567890', 'Y' ),
     ( 'f5279b7f-c83f-4494-bb76-abb0059d5d3e', 10, 'Andy', 'Reising', 'areising@westmonroe.com', '1234567890', 'Y' ),
     ( '81b44c63-ef20-495b-bceb-b3dd191a5b47', 10, 'Ari', 'Russo', 'arusso@westmonroe.com', '1234567890', 'Y' ),
     ( 'c519e625-f218-4bbd-b5b9-acbb7c602995', 10, 'Justin', 'Passanisi', 'jpassanisi+lmdev@westmonroe.com', '1234567890', 'Y' ),
     ( 'c66b744f-d96f-450f-88a3-064749e470be', 10, 'Gabriel', 'Arce', 'garce@westmonroe.com', '1234567890', 'Y' ),
     ( 'b5fc4bcf-d970-4caa-9642-56311585c032', 10, 'Ifeanyi', 'Iyke-Azubogu', 'iiykeazubogu@westmonroe.com', '1234567890', 'Y' ),
     ( 'd3be5314-17cf-4af5-8cb0-965a5481cc28', 10, 'Drew', 'Dado', 'ddado@westmonroe.com', '1234567890', 'Y' ),
     ( 'afd821dd-8cca-4292-926c-754dfc130b2c', 10, 'Jeremy', 'Green', 'jegreen@westmonroe.com', '1234567890', 'Y' ),
     ( '2df979c6-8288-4964-9d84-5aa740c416d7', 10, 'Griffin', 'Kosonocky', 'gkosonocky+lmdev@westmonroe.com', '1234567890', 'Y' ),
     ( 'a4811b2b-f2fa-4812-8a30-85416145986c', 10, 'Claire', 'Norman', 'cnorman@westmonroe.com', '1234567890', 'Y' ),
     ( 'fec29793-5296-4f90-aa09-cc21fdfcfec1', 10, 'Jonathan', 'Brotherton', 'jbrotherton@westmonroe.com', '1234567890', 'Y' ),
     ( 'fec29793-5296-4f90-aa09-cc21fdfcfed1', 10, 'Raz', 'Consta', 'rconstantinescu+lmdev@westmonroe.com', '', 'Y' ),
     ( 'dec29793-5296-4f90-aa09-cc21fdfcfef3', 10, 'Gabriel', 'Lopez', 'galopez+lmdev@westmonroe.com', '', 'Y' ),
     ( 'aec29793-5296-4f90-ba09-cc21fdfcfff4', 10, 'Luke', 'Polishak', 'lpolishak+lmdev@westmonroe.com', '', 'Y');

------------------------------------
--  Create West Monroe User Roles --
------------------------------------
-- This query adds all roles for our users, except for the SUPER_ADM role.
INSERT INTO USER_ROLE_XREF
     ( USER_ID, ROLE_ID )
       SELECT UI.USER_ID
            , RD.ROLE_ID
         FROM USER_INFO UI
            , ROLE_DEF RD
        WHERE UI.USER_ID >= ( SELECT USER_ID FROM USER_INFO WHERE USER_UUID = 'd1091f7d-b94f-4a45-afc7-1d33580e8a0c' )
          AND RD.ROLE_ID > 1 AND RD.ROLE_ID < 14;

-- This gives Cole Mitchell DEAL_INV_RECIP role for the West Monroe institution
INSERT INTO USER_ROLE_XREF
     ( USER_ID, ROLE_ID )
       VALUES
     ( 28, 14 );

-----------------------------------------------------
-- Create United Farm Credit Services (Lead) Users --
-----------------------------------------------------
INSERT INTO USER_INFO
     ( USER_UUID, INSTITUTION_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDR, PASSWORD_DESC, ACTIVE_IND )
       VALUES
     ( '1de8fce1-4fbd-4b95-932b-654efe2096fa', 5, 'Griffin', 'Kosonocky', 'gkosonocky+lmorig@westmonroe.com', '1234567890', 'Y' ),
     ( '67c863ea-8633-405d-bb4c-ec526a15483b', 5, 'Jayna', 'Curran', 'jcurran@westmonroe.com', '1234567890', 'Y' ),
     ( '4d3ae45a-1146-40e9-be91-d98c16b640b9', 5, 'Corey', 'Coscioni', 'ccoscioni@westmonroe.com', '1234567890', 'Y' ),
     ( '2b444b99-4864-4d34-8ed6-8b1ba258b7b6', 5, 'Andy', 'Reising', 'areising2@westmonroe.com', '1234567890', 'Y' ),
     ( 'e2693891-f223-4f0f-830d-68601cd0b745', 5, 'Jeremy', 'Green', 'jgreen@westmonroe.com', '1234567890', 'Y' ),
     ( 'b667c272-f082-439f-9294-55f81e4888ee', 5, 'Jonathan', 'Brotherton', 'jbrotherton2@westmonroe.com', '1234567890', 'Y' ),
     ( 'f9d34e5b-2a2f-43fe-b6e2-d1064e7e6487', 5, 'Justin', 'Passanisi', 'jpassanisi+lmorig@westmonroe.com', '1234567890', 'Y' );

-------------------------------------------------
-- Create United Farm Credit User (Lead) Roles --
-------------------------------------------------
INSERT INTO USER_ROLE_XREF
     ( USER_ID, ROLE_ID )
       SELECT UI.USER_ID
            , RD.ROLE_ID
       FROM USER_INFO UI
          , ROLE_DEF RD
       WHERE UI.USER_ID >= ( SELECT USER_ID FROM USER_INFO WHERE USER_UUID = '1de8fce1-4fbd-4b95-932b-654efe2096fa' )
         AND RD.ROLE_ID > 1 AND RD.ROLE_ID < 14;

----------------------------------------------
-- Create Prairie Land (Participants) Users --
----------------------------------------------
INSERT INTO USER_INFO
     ( USER_UUID, INSTITUTION_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDR, PASSWORD_DESC, ACTIVE_IND )
       VALUES
     ( '6a784da8-8b64-4b27-aeb7-2903a96e4a4c', 6, 'Griffin', 'Kosonocky', 'gkosonocky+lmpart1@westmonroe.com', '1234567890', 'Y' ),
     ( '90637a18-74c2-4246-94e9-735b97649441', 6, 'Jayna', 'Curran', 'jcurran2@westmonroe.com', '1234567890', 'Y' ),
     ( 'd2e42b4e-7740-4474-9114-a0ac1058e78c', 6, 'Corey', 'Coscioni', 'ccoscioni2@westmonroe.com', '1234567890', 'Y' ),
     ( 'bf223253-2a92-4bb0-840a-3868d3d70877', 6, 'Andy', 'Reising', 'areising3@westmonroe.com', '1234567890', 'Y' ),
     ( '6248d9d4-5453-41d3-bc56-e399b7730431', 6, 'Jeremy', 'Green', 'jgreen2@westmonroe.com', '1234567890', 'Y' ),
     ( 'd962a336-2acd-4911-83c2-67c3b5ebbd94', 6, 'Jonathan', 'Brotherton', 'jbrotherton3@westmonroe.com', '1234567890', 'Y' ),
     ( '068defdb-b4b4-47aa-9a1b-7f38d0563f0d', 6, 'Justin', 'Passanisi', 'jpassanisi+lmpart1@westmonroe.com', '1234567890', 'Y' );

--------------------------------------------------
-- Create Prairie Land User (Participant) Roles --
--------------------------------------------------
-- This query adds ALL roles (minus Super Admin and App Service) for the Prairie Land users.
INSERT INTO USER_ROLE_XREF
     ( USER_ID, ROLE_ID )
       SELECT UI.USER_ID
            , RD.ROLE_ID
         FROM USER_INFO UI
            , ROLE_DEF RD
        WHERE UI.USER_ID >= ( SELECT USER_ID FROM USER_INFO WHERE USER_UUID = '6a784da8-8b64-4b27-aeb7-2903a96e4a4c' )
          AND RD.ROLE_ID > 1 AND RD.ROLE_ID < 14;

--------------------------------------------------
-- Create West Monroe Confidentiality Agreement --
--------------------------------------------------
INSERT INTO INSTITUTION_CONFIDENTIALITY_AGREEMENT
     ( INSTITUTION_ID, CREATED_BY_ID, CONF_AGRMNT_DESC )
       VALUES
     ( 10, 28, 'West Monroe Credit Association Confidentiality Agreement' );