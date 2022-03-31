CREATE PROCEDURE NoweZlecenie @PacjentID INT, @RodzajSzczepionki varchar(30), @CentrumSzczepienID INT, @Data DATE
AS
BEGIN

--sprawdzenie czy jest ta szczepionka 
	DECLARE @DostepnaPartia INT = (
	SELECT TOP 1 ps.PartiaID FROM CentraSzczepien cs
	JOIN Zamowienia z ON cs.CentrumSzczepienID=z.CentrumSzczepienID
	JOIN SzczegolyZamowien sz ON sz.ZamowienieID=z.ZamowienieID
	JOIN PartieSzczepionki ps ON ps.PartiaID=sz.PartiaID
	LEFT JOIN ZlecenieSzczepienia zs ON zs.PartiaID=ps.PartiaID
	WHERE DataWaznosci > @Data
	AND ISNULL(DataSzczepienia, 0) < @Data
	AND cs.CentrumSzczepienID = @CentrumSzczepienID 
	AND  SzczepionkaID = (
		SELECT SzczepionkaID FROM Szczepionki WHERE Firma = @RodzajSzczepionki
	) AND (Status IS NULL OR Status = 'zrealizowane' OR Status = 'oczekujace')
	GROUP BY cs.CentrumSzczepienID, ps.PartiaID, SzczepionkaID
	HAVING MAX(LiczbaDawek) - COUNT(PacjentID) > 0
)
-- jesli nie ma
IF LEN(@DostepnaPartia) IS NULL
BEGIN
	--sprawdzenie czy szczepionka jest w magazynie
	DECLARE @PartaMag INT = (
	SELECT TOP 1 PartiaID FROM PartieSzczepionki
	WHERE PartiaID NOT IN (SELECT DISTINCT PartiaID FROM SzczegolyZamowien)
	AND SzczepionkaID = (SELECT SzczepionkaID FROM Szczepionki WHERE Firma = @RodzajSzczepionki)
	AND DataWaznosci > @Data
	)
	--jesli tak to rezerwacja - dodanie zamowienia na szczepionke
	IF LEN(@PartaMag) IS NOT NULL
	BEGIN
		INSERT INTO Zamowienia (CentrumSzczepienID, DataZamowienia) VALUES(@CentrumSzczepienID, GETDATE())
		INSERT INTO SzczegolyZamowien (ZamowienieID, PartiaID) VALUES ((SELECT MAX(ZamowienieID) FROM Zamowienia), @PartaMag)
	END
END
--jesli szczepionka jest dostepna w wybranym centrum
ELSE
BEGIN
	--sprawdzenie ile dawek przyjal pacjent
	DECLARE @Dawka INT = 
	(
		SELECT MAX(ISNULL(Dawka, 0)) FROM Pacjenci p 
		LEFT JOIN ZlecenieSzczepienia zs  ON zs.PacjentID = p.PacjentID
		WHERE (Status != 'odwolane' OR Status IS NULL) AND p.PacjentID= @PacjentID
		GROUP BY p.PacjentID
	)
	--jesli  0 to zapis na 1
	IF @Dawka = 0 AND EXISTS (
		SELECT * FROM Pacjenci p 
		LEFT JOIN ZlecenieSzczepienia zs  ON zs.PacjentID = p.PacjentID
		LEFT JOIN Opoznienia o ON o.PacjentID=p.PacjentID
		WHERE p.PacjentID=8 AND DATEADD(DAY, ISNULL(Czas, 0), ISNULL(o.DataOpoznienia, '1900-01-01')) < @Data
	)
	BEGIN
		INSERT INTO ZlecenieSzczepienia VALUES
		(@DostepnaPartia, @PacjentID, @Data, 'oczekujace', 1)
	END
	--jesli 1 to zapis na 2
	IF @Dawka = 1 AND EXISTS (
		SELECT * FROM Pacjenci p 
		LEFT JOIN ZlecenieSzczepienia zs  ON zs.PacjentID = p.PacjentID
		LEFT JOIN Opoznienia o ON o.PacjentID=p.PacjentID
		JOIN PartieSzczepionki ps ON ps.PartiaID=zs.PartiaID
		JOIN Szczepionki s ON s.SzczepionkaID = ps.SzczepionkaID
		WHERE p.PacjentID=7 AND Status = 'zrealizowane'
		AND DATEADD(DAY, ISNULL(Czas, 0), ISNULL(o.DataOpoznienia, '1900-01-01')) < @Data
		AND DATEADD(DAY, MinCzasMiedzyDawkami, DataSzczepienia) < @Data
	)
	BEGIN
		INSERT INTO ZlecenieSzczepienia VALUES
		(@DostepnaPartia, @PacjentID, @Data, 'oczekujace', 2)
	END
END
END