IF NOT EXISTS (SELECT * FROM sys.syslogins WHERE name = N'RapTAT_user')
BEGIN
	CREATE LOGIN RapTAT_user WITH PASSWORD = 'raptat_USER';
END;

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = N'RapTAT_user')
BEGIN
    CREATE USER [RapTAT_user] FOR LOGIN [RapTAT_user]
    EXEC sp_addrolemember N'db_owner', N'RapTAT_user'
END;