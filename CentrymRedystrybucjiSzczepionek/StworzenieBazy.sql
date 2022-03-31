USE master
GO
if exists (select * from sysdatabases where name='CentrumRedystrybucjiSzczepionek')
		drop database CentrumRedystrybucjiSzczepionek
GO
CREATE DATABASE CentrumRedystrybucjiSzczepionek;
GO
USE CentrumRedystrybucjiSzczepionek;
GO

CREATE TABLE Pacjenci(
	"PacjentID" INT NOT NULL PRIMARY KEY CLUSTERED IDENTITY (1,1),
	"Imie" VARCHAR(20) NOT NULL,
	"Nazwisko" VARCHAR(30) NOT NULL,
	"Pesel" VARCHAR(11) NOT NULL,
	"DataUrodzenia" DATE NOT NULL,
	"MiastoUrodzenia" VARCHAR(30) NOT NULL,
	"Przeciwskazania" BIT,
	"Kategoria" SMALLINT NOT NULL, 
	CONSTRAINT "Pesel" CHECK ( LEN("Pesel") = 11 ),
	CONSTRAINT "DataUrodzenia" CHECK( "DataUrodzenia" <= GETDATE() ),
	CONSTRAINT "Kategoria" CHECK( "Kategoria" >= 0 AND "Kategoria" <= 4 )
)

CREATE TABLE Opoznienia(
	"OpoznienieID" INT NOT NULL PRIMARY KEY CLUSTERED IDENTITY (1, 1),
	"PacjentID" INT NOT NULL,
	"Dawka" INT NOT NULL,
	"Powod" VARCHAR(30),
	"Czas" INT NOT NULL,
	"DataOpoznienia" DATE NOT NULL,
	CONSTRAINT "FK_Opoznienie_PacjentID" FOREIGN KEY ("PacjentID") REFERENCES Pacjenci("PacjentID"),
	CONSTRAINT "OpoznionaDawka" CHECK( Dawka=1 OR Dawka=2 ),
	CONSTRAINT "DataOpoznienia" CHECK( "DataOpoznienia" <= GETDATE() )
)

CREATE TABLE Szczepionki(
	"SzczepionkaID" INT NOT NULL PRIMARY KEY CLUSTERED IDENTITY (1, 1),
	"Firma" VARCHAR(30) NOT NULL,
	"CzasWaznosci" INT NOT NULL,
	"MinTemperatura" FLOAT NOT NULL,
	"MaxTemperatura" FLOAT NOT NULL,
	"MinCzasMiedzyDawkami" INT NOT NULL,
	"MaxCzasMiedzyDawkami" INT NOT NULL,
	CONSTRAINT "MaxTemperatura" CHECK( "MaxTemperatura" > -273 ),
	CONSTRAINT "MinTemperatura" CHECK( "MinTemperatura" > -273 )
)

CREATE TABLE PartieSzczepionki(
	"PartiaID" INT NOT NULL PRIMARY KEY CLUSTERED IDENTITY (1,1),
	"SzczepionkaID" INT NOT NULL,
	"LiczbaDawek" INT NOT NULL,
	"Fabryka" VARCHAR(30),
	"DataProdukcji" DATE NOT NULL,
	CONSTRAINT "FK_Szczepionki_SzczepionkaID" FOREIGN KEY ("SzczepionkaID") REFERENCES Szczepionki("SzczepionkaID"),
	CONSTRAINT "DataProdukcji" CHECK( "DataProdukcji" <= GETDATE() )
)


CREATE TABLE CentraSzczepien(
	"CentrumSzczepienID" INT NOT NULL PRIMARY KEY CLUSTERED IDENTITY (1, 1),
	"Miasto" VARCHAR(30) NOT NULL,
	"Ulica" VARCHAR(30)  NOT NULL,
	"NumerBudynku" INT NOT NULL,
	"KodPocztowy" VARCHAR(6) NOT NULL,
	CONSTRAINT "KodPocztowy" CHECK ( LEN("KodPocztowy") = 6 )
)

/*
CREATE TABLE Zamowienia(
	"PartiaID" INT  NOT NULL,
	"CentrumSzczepienID" INT NOT NULL,
	"LiczbaPartii" INT NOT NULL,
	"DataZamowienia" DATE NOT NULL,
	CONSTRAINT "FK_Partie_Zamowienie_PartiaID" FOREIGN KEY ("PartiaID") REFERENCES PartieSzczepionki("PartiaID"),
	CONSTRAINT "FK_CentraSzczepien_CentrumSzczepienID" FOREIGN KEY ("CentrumSzczepienID") REFERENCES CentraSzczepien("CentrumSzczepienID"),
	CONSTRAINT "DataZamowienia" CHECK( "DataZamowienia" <= GETDATE() )
)
*/


CREATE TABLE ZlecenieSzczepienia(
	"PartiaID" INT NOT NULL,
	"PacjentID" INT NOT NULL,
	"DataSzczepienia" DATETIME NOT NULL,
	"Status" VARCHAR(12) NOT NULL,
	"Dawka" INT NOT NULL,
	CONSTRAINT "FK_Partie_Zlecenie_PartiaID" FOREIGN KEY ("PartiaID") REFERENCES PartieSzczepionki("PartiaID"),
	CONSTRAINT "FK_Pacjenci_PacjentID" FOREIGN KEY ("PacjentID") REFERENCES Pacjenci("PacjentID"),
	CONSTRAINT "Status" CHECK( "Status" = 'zrealizowane' OR "Status" = 'oczekujace' OR "Status" = 'odwolane' ),
	CONSTRAINT "Dawka" CHECK( "Dawka" >= 1 OR "Dawka" <= 2 )
)

CREATE TABLE Zamowienia(
	"ZamowienieID" INT NOT NULL PRIMARY KEY CLUSTERED IDENTITY(1, 1),
	"CentrumSzczepienID" INT NULL,
	"DataZamowienia" DATE NOT NULL,
	"DataWysylki" DATE NULL,
	CONSTRAINT "DataZamowienia" CHECK( "DataZamowienia" <= GETDATE() ),
	CONSTRAINT "DataWysylki" CHECK( "DataWysylki" <= GETDATE() ),
	CONSTRAINT FK_CentraSzczepien_CentrumID FOREIGN KEY ("CentrumSzczepienID") REFERENCES CentraSzczepien("CentrumSzczepienID")
)

CREATE TABLE SzczegolyZamowien(
	"ZamowienieID" INT NOT NULL,
	"PartiaID" INT  NOT NULL,
	CONSTRAINT "FK_Partie_Zamowienie_PartiaID" FOREIGN KEY ("PartiaID") REFERENCES PartieSzczepionki("PartiaID"),
	CONSTRAINT "FK_Zamowienia_ZamowienieID" FOREIGN KEY ("ZamowienieID") REFERENCES Zamowienia("ZamowienieID")
)

--ALTER INDEX ALL ON Pacjenci DISABLE
--ALTER INDEX ALL ON Opoznienia DISABLE
--ALTER INDEX ALL ON PartieSzczepionki DISABLE
--ALTER INDEX ALL ON Szczepionki DISABLE
--ALTER INDEX ALL ON Zamowienia DISABLE
--ALTER INDEX ALL ON CentraSzczepien DISABLE

--CREATE CLUSTERED INDEX IND_PK_Pacjenci_PacjentID ON Pacjenci("PacjentID" ASC)
--CREATE UNIQUE CLUSTERED INDEX IND_PK_Opoznienia_OpoznienieID ON Opoznienia("OpoznienieID" ASC)
--CREATE UNIQUE CLUSTERED INDEX IND_PK_PartieSzczepionki_PartiaID ON PartieSzczepionki("PartiaID" ASC)
--CREATE UNIQUE CLUSTERED INDEX IND_PK_Szczepionki_SzczepionkaID ON Szczepionki("SzczepionkaID" ASC)
--CREATE UNIQUE CLUSTERED INDEX IND_PK_Zamowienia_ZamowienieID ON Zamowienia("ZamowienieID" ASC)
--CREATE UNIQUE CLUSTERED INDEX IND_PK_CentraSzczepien_CentrumSzczepienID ON CentraSzczepien("CentrumSzczepienID" ASC) 

CREATE NONCLUSTERED INDEX IND_FK_Opoznienia_PacjentID ON Opoznienia("PacjentID")
CREATE NONCLUSTERED INDEX IND_FK_ZlecenieSzczepienia_PacjentID ON ZlecenieSzczepienia("PacjentID")
CREATE NONCLUSTERED INDEX IND_FK_ZlecenieSzczepienia_PartiaID ON ZlecenieSzczepienia("PartiaID")
CREATE NONCLUSTERED INDEX IND_FK_PartieSzczepionki_SzczepionkaID ON PartieSzczepionki("SzczepionkaID")
CREATE NONCLUSTERED INDEX IND_FK_SzczegolyZamowien_PartiaID ON SzczegolyZamowien("PartiaID")
CREATE NONCLUSTERED INDEX IND_FK_SzczegolyZamowien_ZamowienieID ON SzczegolyZamowien("ZamowienieID")
CREATE NONCLUSTERED INDEX IND_FK_Zamowienia_CentrumSzczepeinID ON Zamowienia("CentrumSzczepienID")
CREATE NONCLUSTERED INDEX IND_FK_Szczepionki_Firma ON Szczepionki("Firma")


INSERT INTO Pacjenci VALUES
('Jan', 'Nowak', '71032250188', '1971-03-22', 'Radom', 0, 2),
('Robert', 'Lewandowski', '83051355456', '1983-05-13', 'Karczew', 0, 4),
('Robert', 'Kubica', '83060150442', '1983-06-01', 'Sosnowiec', 1, 3),
('Laura', 'Bobas', '77060315460', '1977-06-03', 'Warszawa', 0, 0),
('Zbyszek', 'Leszek', '62070539045', '1962-07-05', 'Kraków', 0, 1),
('Ola', 'Kowalska', '82090144822', '1982-09-01', 'Warszawa', 0, 3),
('Marcin', 'Prokop', '93011817708', '1993-01-18', 'Warszawa', 1, 4),
('Wojciech', 'Kowalczyk', '95010111111', '1995-01-01', 'Warszawa', 0, 4)

UPDATE Pacjenci SET Imie = 'Jakub', DataUrodzenia = '1999-10-19' WHERE PacjentID = 1
UPDATE Pacjenci SET Pesel = '84070120444', MiastoUrodzenia = 'Bytom' WHERE PacjentID = 3
UPDATE Pacjenci SET Nazwisko = 'Najman', DataUrodzenia = '1985-01-02', Kategoria = 3 WHERE PacjentID = 7

INSERT INTO Opoznienia VALUES
(3, 1, 'wirus', 14, '2021-03-03'),
(6, 2, 'kwarantanna', 14, '2021-04-12')

INSERT INTO Szczepionki VALUES
('Pfizer', 80, -60, -30, 15, 60),
('Moderna', 90, 3, 23, 10, 55),
('AstraZeneca', 100, 10, 20, 20, 30),
('Johnson&Johnson', 45, 5, 30, 14, 21),
('Winiary', 29, -10, 50, 28, 35)

INSERT INTO PartieSzczepionki (SzczepionkaID, LiczbaDawek, Fabryka, DataProdukcji) VALUES
(1, 100, 'Pfizer Warszawa', '2021-01-14'),
(2, 50, 'Moderna Berlin', '2021-01-17'),
(3, 45, 'AstraZeneca Madryt', '2021-02-05'),
(4, 30, 'Johnson&Johnson Londyn', '2021-02-28'),
(5, 100, 'Wininary Kielce', '2021-03-04'),
(1, 200, 'Pfizer Warszawa', '2021-03-07'),
(2, 60, 'Moderna Lublin', '2021-03-26'),
(3, 75, 'AstraZeneca £ódŸ', '2021-04-01'),
(4, 85, 'Johnson&Johnson Lublin', '2021-04-14'),
(5, 40, 'Winiary Kielce', '2021-04-17'),
(1, 400, 'Pfizer Warszawa', '2021-04-19'),
(1, 50, 'Pfizer Gdañsk', '2021-04-28'),
(2, 200, 'Modrna Berlin', '2021-05-12'),
(1, 200, 'Pfizer Gdañsk', '2021-05-18'),
(2, 40, 'Moderna Lublin', '2021-05-20'),
(3, 50, 'AstraZeneca £ódŸ', '2021-05-25')


INSERT INTO CentraSzczepien VALUES
('Warszawa', 'Wawelska', 44, '03-420'),
('Radom', 'Majowa', 53, '87-648'),
('Gliwice', 'D³uga', 2, '12-234'),
('Warszawa', 'Piêkna', 29, '01-001'),
('Kraków', 'Mokra', 9, '32-543')



INSERT INTO Zamowienia VALUES
(1, '2021-01-13', '2021-01-18'),
(2, '2021-02-02', '2021-02-05'),
(3, '2021-02-25', '2021-03-07'),
(4, '2021-03-21', '2021-04-01'),
(5, '2021-04-14', '2021-04-19'),
(1, '2021-04-27', '2021-04-29'),
(2, '2021-05-11', '2021-05-12'),
(1, '2021-05-17', '2021-05-19')

INSERT INTO SzczegolyZamowien VALUES
(1, 1),
(1, 2),
(2, 3),
(3, 4),
(3, 5),
(3, 6),
(4, 7),
(4, 8),
(5, 9),
(5, 10),
(5, 11),
(6, 12),
(7, 13),
(8, 14)



INSERT INTO ZlecenieSzczepienia VALUES
(1, 4, '2021-01-21 08:15:00', 'zrealizowane', 1),
(1, 4, '2021-02-20 10:30:00', 'zrealizowane', 2),
(1, 5, '2021-01-29 09:45:00', 'zrealizowane', 1),
(2, 5, '2021-03-02 08:00:00', 'zrealizowane',2),
(1, 1, '2021-02-10 09:00:00', 'zrealizowane', 1),
(6, 1, '2021-03-15 09:00:00', 'zrealizowane', 2),
(2, 3, '2021-03-05 21:00:00', 'odwolane', 1),
(2, 3, '2021-03-22 12:30:00', 'zrealizowane', 1),
(13, 3, '2021-05-13 14:55:00', 'oczekujace', 2),
(6, 6, '2021-03-15 18:00:00', 'zrealizowane', 1),
(6, 6, '2021-04-15 20:00:00', 'odwolane', 2),
(11, 6, '2021-05-15 13:00:00', 'oczekujace', 2),
(6, 2, '2021-04-18 11:55:00', 'zrealizowane', 1),
(12, 2, '2021-05-20 12:40:00', 'oczekujace', 2),
(11, 7, '2021-05-03 16:00:00', 'zrealizowane', 1)


ALTER TABLE PartieSzczepionki ADD DataWaznosci DATE NULL

UPDATE PartieSzczepionki SET DataWaznosci=(
	SELECT DATEADD(DAY, CzasWaznosci, DataProdukcji) AS DataWaznosci  FROM PartieSzczepionki ps
	JOIN Szczepionki s ON s.SzczepionkaID=ps.SzczepionkaID WHERE PartieSzczepionki.PartiaID = ps.PartiaID
	)
	

/*
CREATE FUNCTION tak()
RETURNS TABLE
RETURN 
SELECT DATEADD(DAY, s.wav, ps.dat) 
	FROM CentraSzczepien ps 
	JOIN NIEWOME s ON ps.CentrumSzczepienID = s.ind
*/