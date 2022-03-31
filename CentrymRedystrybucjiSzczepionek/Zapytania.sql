SELECT * FROM CentraSzczepien
SELECT * FROM Pacjenci
SELECT * FROM PartieSzczepionki
SELECT * FROM Szczepionki
SELECT * FROM Zamowienia
SELECT * FROM SzczegolyZamowien
SELECT * FROM ZlecenieSzczepienia
SELECT * FROM Opoznienia

--1
SELECT COUNT(*) AS LiczbaSzczepienWPrzyszlymTygodniu 
FROM ZlecenieSzczepienia 
WHERE DataSzczepienia >= GETDATE() 
AND DataSzczepienia <= DATEADD(DAY, 7, DataSzczepienia) 
AND Status = 'oczekujace'


--2
SELECT TOP 1 CentrumSzczepienID, ROUND(CAST(SUM(ZakupioneDawki) - SUM(PodaneDawki) AS FLOAT) 
/ CAST(SUM(ZakupioneDawki) AS FLOAT), 4) * 100 AS ProcentNiewykorzytsanych FROM (
	SELECT cs.CentrumSzczepienID, MAX(LiczbaDawek) AS ZakupioneDawki,
	COUNT(PacjentID) AS PodaneDawki 
	FROM CentraSzczepien cs
	JOIN Zamowienia z ON cs.CentrumSzczepienID=z.CentrumSzczepienID
	JOIN SzczegolyZamowien sz ON sz.ZamowienieID=z.ZamowienieID
	JOIN PartieSzczepionki ps ON ps.PartiaID=sz.PartiaID
	LEFT JOIN ZlecenieSzczepienia zs ON zs.PartiaID=ps.PartiaID
	WHERE DataWaznosci < GETDATE()
	AND (Status IS NULL OR Status = 'zrealizowane')
	GROUP BY cs.CentrumSzczepienID, ps.PartiaID
) AS x
GROUP BY CentrumSzczepienID
ORDER BY ProcentNiewykorzytsanych DESC

--3
SELECT Firma, SUM(LiczbaDawek) AS LacznaLiczbaDawek FROM Szczepionki s JOIN
PartieSzczepionki ps ON s.SzczepionkaID=ps.SzczepionkaID
GROUP BY Firma


--4
SELECT Imie, Nazwisko, o.Dawka, DataSzczepienia, Powod, Kategoria FROM Pacjenci p 
JOIN ZlecenieSzczepienia zs ON p.PacjentID=zs.PacjentID 
JOIN Opoznienia o ON o.PacjentID=p.PacjentID
WHERE  zs.Dawka = 1 AND Status = 'zrealizowane' AND DataSzczepienia < GETDATE() 
AND o.Dawka=2 AND Powod='kwarantanna'
ORDER BY Kategoria


--5
SELECT Fabryka, Firma, COUNT(*) AS LiczbaWykorzytanychSzczepionek 
FROM Szczepionki s 
JOIN PartieSzczepionki ps ON s.SzczepionkaID=ps.SzczepionkaID
JOIN ZlecenieSzczepienia zs ON zs.PartiaID=ps.PartiaID
WHERE Status='zrealizowane'
GROUP BY Fabryka, Firma
ORDER BY LiczbaWykorzytanychSzczepionek DESC


--rejestracja na 1 dawke szczepienia
BEGIN TRANSACTION
SELECT * FROM ZlecenieSzczepienia WHERE PacjentID = 8
EXEC NoweZlecenie @PacjentID = 8, @RodzajSzczepionki = 'Pfizer', @CentrumSzczepienID = 1, @Data = '2021-06-01';
SELECT * FROM ZlecenieSzczepienia WHERE PacjentID = 8
ROLLBACK

--rejestracja na 2 dawke szczepienia
BEGIN TRANSACTION
SELECT * FROM ZlecenieSzczepienia WHERE PacjentID = 7
EXEC NoweZlecenie @PacjentID = 7, @RodzajSzczepionki = 'Pfizer', @CentrumSzczepienID = 1, @Data = '2021-06-01';
SELECT * FROM ZlecenieSzczepienia WHERE PacjentID = 7
ROLLBACK

--rejestracja na 1 dawke szczepienia pacjenta po szczepeiniu
BEGIN TRANSACTION
SELECT * FROM ZlecenieSzczepienia WHERE PacjentID = 1
EXEC NoweZlecenie @PacjentID = 8, @RodzajSzczepionki = 'Pfizer', @CentrumSzczepienID = 1, @Data = '2021-06-01';
SELECT * FROM ZlecenieSzczepienia WHERE PacjentID = 1
ROLLBACK


--zamowienie szczepionki z magazynu
BEGIN TRANSACTION
SELECT * FROM Zamowienia WHERE CentrumSzczepienID = 3
SELECT * FROM SzczegolyZamowien
EXEC NoweZlecenie @PacjentID = 1, @RodzajSzczepionki = 'Moderna', @CentrumSzczepienID = 3, @Data = '2021-06-01';
SELECT * FROM Zamowienia WHERE CentrumSzczepienID = 3
SELECT * FROM SzczegolyZamowien
ROLLBACK