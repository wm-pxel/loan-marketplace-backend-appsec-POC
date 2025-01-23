CREATE TABLE BILLING_DEF (
    BILLING_CD                  VARCHAR(25) NOT NULL,
    BILLING_DESC                VARCHAR(255) NOT NULL,
    PRIMARY KEY ( BILLING_CD )
);

INSERT INTO BILLING_DEF (BILLING_CD, BILLING_DESC)
VALUES
    ('API', 'Buy Side with API Access'),
    ('ENTERPRISE', 'Enterprise'),
    ('FREE', 'Buy Side Only'),
    ('ENTAPI', 'Enterprise with API Access');

CREATE TABLE END_USER_AGREEMENT (
    EUA_ID                          SERIAL NOT NULL,
    BILLING_CD                      VARCHAR(25) NOT NULL,
    EUA_CONTENT                     VARCHAR(10000) NULL,
    CREATED_DATE                    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( EUA_ID ),
    FOREIGN KEY ( BILLING_CD ) REFERENCES BILLING_DEF ( BILLING_CD )
);

CREATE TABLE USER_EUA_XREF (
    USER_ID                         INTEGER NOT NULL,
    EUA_ID                          INTEGER NOT NULL,
    AGREEMENT_DATE                  TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY ( USER_ID, EUA_ID ),
    FOREIGN KEY ( USER_ID ) REFERENCES USER_INFO ( USER_ID ),
    FOREIGN KEY ( EUA_ID ) REFERENCES END_USER_AGREEMENT ( EUA_ID )
);

ALTER TABLE INSTITUTION_INFO
    ADD BILLING_CD VARCHAR(25) NOT NULL DEFAULT 'FREE',
    ADD FOREIGN KEY ( BILLING_CD ) REFERENCES BILLING_DEF (BILLING_CD);

INSERT INTO END_USER_AGREEMENT
    ( BILLING_CD, EUA_CONTENT )
    VALUES
    ('FREE', 'Lorem ipsum odor amet, consectetuer adipiscing elit. Nibh purus aptent sodales a potenti. Commodo sagittis duis odio sem, per eleifend dui. Mus lobortis lobortis dictum felis ac urna mattis ultrices. Odio leo mus penatibus cras molestie integer condimentum. Laoreet tempor venenatis nunc felis a sodales duis elementum senectus. Fermentum elit fringilla sagittis pellentesque magna gravida; fermentum adipiscing vehicula. Sem augue faucibus eleifend vulputate iaculis mus.
Dignissim suscipit platea per, tempor gravida ac est nisl. Vulputate dui vestibulum placerat interdum litora massa varius. Sed ridiculus sagittis nec torquent; at lacinia finibus dui. Magnis amet proin erat neque dis pharetra vel suscipit. Molestie pharetra nunc platea morbi diam vel. Morbi mollis accumsan sagittis; efficitur tristique est nunc nibh consectetur. Mattis suspendisse posuere proin, ultrices potenti dignissim. Class faucibus vehicula dictumst sodales vehicula sapien congue aliquam.
Vivamus nunc pretium erat massa vel montes magnis. At ultricies iaculis potenti fusce eleifend. Posuere tortor himenaeos non hendrerit proin. Justo lectus vitae leo sollicitudin rutrum curae. Per imperdiet ipsum ante aptent auctor. Varius rutrum conubia duis tristique donec.
Risus luctus non phasellus sit tempor praesent risus. Conubia tellus porta est, justo sodales porta parturient. Habitant tempus tincidunt posuere elit hendrerit nec. Netus imperdiet egestas rhoncus phasellus ex dolor. Ullamcorper sed risus posuere metus nostra aptent convallis sem. Vestibulum eros maecenas dis vehicula mauris fermentum etiam integer et. Fringilla sagittis pharetra lacus integer lacus penatibus lacinia fermentum. Eleifend purus rutrum curae malesuada hendrerit maecenas. Facilisis mi sed aptent porta feugiat class.
Ligula eget nibh tincidunt facilisi, tempor tristique lacus quis. Turpis magna per in mus justo cubilia ligula ullamcorper. Aliquet egestas pellentesque eros praesent fusce. Dapibus felis himenaeos purus senectus vehicula fusce aliquet ullamcorper. Aptent congue sociosqu posuere venenatis conubia viverra. Vitae gravida mauris eget commodo cubilia fringilla aliquet. Ultricies facilisis diam congue tristique integer ad nam bibendum. Iaculis lorem a eros gravida facilisi sapien; porttitor vel!
Tempus magnis mus hendrerit, vestibulum suscipit mattis velit morbi. Sit quisque nisl penatibus conubia montes lectus varius elementum. Fames cubilia quam leo ad gravida curae arcu. Gravida eu phasellus dictum fames nunc tristique nam. Consectetur aliquam semper aptent montes gravida. Lectus praesent volutpat ridiculus nisi ultricies; at taciti. Ante in facilisis risus libero bibendum cursus. Nascetur hendrerit venenatis imperdiet fermentum fringilla fermentum fames. Integer sit pharetra dolor lobortis in, orci himenaeos.
Odio felis lacinia fusce platea primis nostra purus. Etiam amet malesuada suscipit; venenatis torquent adipiscing. Molestie vehicula magnis; netus ad ut potenti. Malesuada penatibus bibendum hendrerit in integer faucibus! Vivamus mollis interdum quam integer quisque posuere urna. Rhoncus scelerisque lectus nisl facilisis primis taciti ridiculus ex. Sollicitudin vivamus proin tortor per enim ad aliquet luctus. Mattis leo habitasse mollis, volutpat aliquam consectetur. Class magna donec ad curabitur semper etiam efficitur.
Tincidunt tincidunt sapien natoque, urna aliquet in. Cubilia maximus vulputate class euismod fermentum enim vestibulum mi aenean. Class sociosqu laoreet viverra litora vitae mus suscipit taciti ornare? Tellus aptent fusce etiam aliquet dictum. Semper venenatis cubilia augue semper ornare lectus erat risus mus. Platea viverra aliquam ullamcorper viverra suspendisse ullamcorper senectus.
Nam nullam primis maecenas platea augue varius. Mattis nulla non dis curabitur gravida lectus nec dolor. Vulputate consequat erat iaculis interdum lobortis dis. Dapibus lobortis condimentum semper ultricies pellentesque proin sit ac. Est lectus ridiculus enim justo aliquet lobortis. Pretium vestibulum himenaeos tempor; nisl posuere blandit. Nisl rutrum eget nascetur sit habitasse donec ligula ullamcorper aliquet.
Ultricies semper dictum erat consequat litora sagittis consectetur nec. Urna varius mus mus ex non vehicula morbi. Per nascetur ultricies curabitur leo turpis fermentum ridiculus luctus. Cubilia ornare massa interdum; ut metus cras amet eleifend. Porttitor urna ridiculus ad facilisi hac massa magnis. Ultrices risus libero platea ridiculus nisl justo. Aliquam pretium gravida platea neque finibus litora tortor lacinia id.senectus.
'),
    ('ENTERPRISE', 'Lorem ipsum odor amet, consectetuer adipiscing elit. Nibh purus aptent sodales a potenti. Commodo sagittis duis odio sem, per eleifend dui. Mus lobortis lobortis dictum felis ac urna mattis ultrices. Odio leo mus penatibus cras molestie integer condimentum. Laoreet tempor venenatis nunc felis a sodales duis elementum senectus. Fermentum elit fringilla sagittis pellentesque magna gravida; fermentum adipiscing vehicula. Sem augue faucibus eleifend vulputate iaculis mus.
Dignissim suscipit platea per, tempor gravida ac est nisl. Vulputate dui vestibulum placerat interdum litora massa varius. Sed ridiculus sagittis nec torquent; at lacinia finibus dui. Magnis amet proin erat neque dis pharetra vel suscipit. Molestie pharetra nunc platea morbi diam vel. Morbi mollis accumsan sagittis; efficitur tristique est nunc nibh consectetur. Mattis suspendisse posuere proin, ultrices potenti dignissim. Class faucibus vehicula dictumst sodales vehicula sapien congue aliquam.
Vivamus nunc pretium erat massa vel montes magnis. At ultricies iaculis potenti fusce eleifend. Posuere tortor himenaeos non hendrerit proin. Justo lectus vitae leo sollicitudin rutrum curae. Per imperdiet ipsum ante aptent auctor. Varius rutrum conubia duis tristique donec.
Risus luctus non phasellus sit tempor praesent risus. Conubia tellus porta est, justo sodales porta parturient. Habitant tempus tincidunt posuere elit hendrerit nec. Netus imperdiet egestas rhoncus phasellus ex dolor. Ullamcorper sed risus posuere metus nostra aptent convallis sem. Vestibulum eros maecenas dis vehicula mauris fermentum etiam integer et. Fringilla sagittis pharetra lacus integer lacus penatibus lacinia fermentum. Eleifend purus rutrum curae malesuada hendrerit maecenas. Facilisis mi sed aptent porta feugiat class.
Ligula eget nibh tincidunt facilisi, tempor tristique lacus quis. Turpis magna per in mus justo cubilia ligula ullamcorper. Aliquet egestas pellentesque eros praesent fusce. Dapibus felis himenaeos purus senectus vehicula fusce aliquet ullamcorper. Aptent congue sociosqu posuere venenatis conubia viverra. Vitae gravida mauris eget commodo cubilia fringilla aliquet. Ultricies facilisis diam congue tristique integer ad nam bibendum. Iaculis lorem a eros gravida facilisi sapien; porttitor vel!
Tempus magnis mus hendrerit, vestibulum suscipit mattis velit morbi. Sit quisque nisl penatibus conubia montes lectus varius elementum. Fames cubilia quam leo ad gravida curae arcu. Gravida eu phasellus dictum fames nunc tristique nam. Consectetur aliquam semper aptent montes gravida. Lectus praesent volutpat ridiculus nisi ultricies; at taciti. Ante in facilisis risus libero bibendum cursus. Nascetur hendrerit venenatis imperdiet fermentum fringilla fermentum fames. Integer sit pharetra dolor lobortis in, orci himenaeos.
Odio felis lacinia fusce platea primis nostra purus. Etiam amet malesuada suscipit; venenatis torquent adipiscing. Molestie vehicula magnis; netus ad ut potenti. Malesuada penatibus bibendum hendrerit in integer faucibus! Vivamus mollis interdum quam integer quisque posuere urna. Rhoncus scelerisque lectus nisl facilisis primis taciti ridiculus ex. Sollicitudin vivamus proin tortor per enim ad aliquet luctus. Mattis leo habitasse mollis, volutpat aliquam consectetur. Class magna donec ad curabitur semper etiam efficitur.
Tincidunt tincidunt sapien natoque, urna aliquet in. Cubilia maximus vulputate class euismod fermentum enim vestibulum mi aenean. Class sociosqu laoreet viverra litora vitae mus suscipit taciti ornare? Tellus aptent fusce etiam aliquet dictum. Semper venenatis cubilia augue semper ornare lectus erat risus mus. Platea viverra aliquam ullamcorper viverra suspendisse ullamcorper senectus.
Nam nullam primis maecenas platea augue varius. Mattis nulla non dis curabitur gravida lectus nec dolor. Vulputate consequat erat iaculis interdum lobortis dis. Dapibus lobortis condimentum semper ultricies pellentesque proin sit ac. Est lectus ridiculus enim justo aliquet lobortis. Pretium vestibulum himenaeos tempor; nisl posuere blandit. Nisl rutrum eget nascetur sit habitasse donec ligula ullamcorper aliquet.
Ultricies semper dictum erat consequat litora sagittis consectetur nec. Urna varius mus mus ex non vehicula morbi. Per nascetur ultricies curabitur leo turpis fermentum ridiculus luctus. Cubilia ornare massa interdum; ut metus cras amet eleifend. Porttitor urna ridiculus ad facilisi hac massa magnis. Ultrices risus libero platea ridiculus nisl justo. Aliquam pretium gravida platea neque finibus litora tortor lacinia id.senectus.
');
