# Talend Dashboard

Application web Spring Boot de tableau de bord pour les exécutions de jobs Talend.  
Elle lit les données d'exécution (stats, logs, meters) depuis des **fichiers locaux**, des **partages réseau** ou une **base de données externe**, et offre une visualisation interactive avec filtres, KPIs, graphiques et pagination.

## 🚀 Fonctionnalités

- **Import multi‑source** : fichiers (locaux, FTP, SFTP, SMB) ou base de données (JDBC) configurable par machine.
- **Tableau de bord interactif** :
  - KPIs (nombre d’exécutions, taux de succès, durée moyenne, erreurs, volume traité).
  - Graphiques (série temporelle, top 5 jobs, succès/échec, volume par job).
  - Tableau des exécutions paginé, triable par colonne.
  - Filtres : machine, job, plage de dates.
  - Sidebar de filtres persistante (sticky).
- **Détails d’une exécution** (popup) :
  - Logs d’erreur (fichier logs ou erreur synthétique pour les échecs).
  - Flow Meters avec pourcentage, référence et seuils colorés (format `tFlowMeter`).
- **Administration** :
  - Ajout / suppression de machines.
  - Choix par machine : **Fichiers** (chemin local ou distant) ou **Base de données** (JDBC + 3 requêtes SQL).
  - Interface épurée avec cartes.
- **Scheduler** : import automatique programmable (expression cron).
- **Import manuel** : bouton dans la barre de navigation.
- **Rafraîchissement automatique** du tableau de bord (toutes les 60 secondes).
- **Suppression en cascade** : suppression d’une machine → suppression de toutes ses données.

## 📦 Prérequis

- **Java 17** ou supérieur.
- **Maven 3.8+**.
- Navigateur moderne (Chrome, Firefox, Edge).

## ⚙️ Installation et démarrage rapide

1. **Cloner le projet** :
   ```bash
   git clone <votre-repo>
   cd talend-dashboard
   ```

2. **Configurer les machines** (optionnel) :  
   Éditez `src/main/resources/application.properties` pour définir une configuration initiale (celle-ci sera recopiée en base au premier démarrage) :
   ```properties
   machines[0].name=DEV
   machines[0].stats.path=/chemin/vers/stats_file.txt
   machines[0].logs.path=/chemin/vers/logs_file.txt
   machines[0].meters.path=/chemin/vers/meter_file.txt
   ```
   > Vous pouvez aussi tout gérer directement via l’interface d’administration après le premier lancement.

3. **Lancer l’application** :
   ```bash
   mvn spring-boot:run
   ```

4. **Accéder au tableau de bord** : [http://localhost:8080](http://localhost:8080)  
   **Interface d’administration** : [http://localhost:8080/admin](http://localhost:8080/admin)

## 🧱 Configuration

Toute la configuration se fait dans `application.properties` :

| Propriété | Description | Défaut |
|-----------|-------------|--------|
| `server.port` | Port HTTP | `8080` |
| `spring.datasource.url` | Base SQLite interne | `jdbc:sqlite:talend-dashboard.db` |
| `spring.jpa.database-platform` | Dialecte Hibernate pour SQLite | `org.hibernate.community.dialect.SQLiteDialect` |
| `spring.jpa.hibernate.ddl-auto` | Mise à jour automatique du schéma | `update` |
| `app.import.cron` | Expression cron pour l’import automatique | `0 0 * * * *` (toutes les heures) |
| `app.upload.dir` | Dossier pour les fichiers uploadés (si utilisé) | `uploads` |
| `machines[x].name` | Nom d’une machine initiale | (vide) |
| `machines[x].stats.path` | Chemin du fichier stats | (vide) |
| `machines[x].logs.path` | Chemin du fichier logs | (vide) |
| `machines[x].meters.path` | Chemin du fichier meters | (vide) |

> 🔐 En production, externalisez les mots de passe et les URL JDBC via des variables d’environnement ou Spring Cloud Config.

## 🗂️ Formats attendus

### Fichier Stats (`stats_file.txt`)
Format Talend `tStatCatcher` (séparateur `;`).
```
moment;pid;father_pid;root_pid;system_pid;project;job;job_repository_id;job_version;context;origin;message_type;message;duration
```
- `message_type` = `begin` ou `end`
- `message` = `success` ou `failure`
- `duration` = durée en ms (pour les lignes `end`)

### Fichier Logs (`logs_file.txt`)
Format Talend `tLogCatcher` (12 colonnes, séparateur `;`).
```
moment;pid;root_pid;father_pid;project;job;context;priority;type;origin;message;code
```

### Fichier Meters (`meter_file.txt`)
Format Talend `tFlowMeter` (15 colonnes, séparateur `;`).
```
moment;pid;father_pid;root_pid;system_pid;project;job;job_repository_id;job_version;context;origin;label;count;reference;thresholds
```
- `thresholds` : format `min|0|30|0|255|0#moy|31|60|255|128|0#max|61|100|255|0|0`

> **Note** : Les fichiers peuvent être stockés localement ou sur un partage réseau (SMB). Pour une base de données externe, utilisez le mode Base de données dans l’administration.

## 🖥️ Utilisation

### Ajouter une machine
1. Aller dans **Administration**.
2. Cliquer sur **Ajouter une machine**.
3. Choisir la source : **Fichiers** ou **Base de données**.
4. Renseigner les champs appropriés (chemins ou connexion JDBC + requêtes SQL).
5. Enregistrer.

### Importer les données
- **Manuel** : cliquer sur le bouton **Import maintenant** dans la barre de navigation.
- **Automatique** : le scheduler importe selon le cron défini (par défaut toutes les heures).

### Consulter le tableau de bord
- Utiliser les filtres (machine, job, dates) pour restreindre l’affichage.
- Les graphiques et KPIs se mettent à jour automatiquement.
- Le tableau des exécutions est paginé et triable (cliquer sur les en‑têtes).
- Cliquer sur **Détails** pour voir les logs d’erreur et les flow meters d’une exécution.

### Supprimer une machine
- Dans l’administration, cliquer sur **Supprimer** sur la carte de la machine.
- **Toutes les données liées** (exécutions, erreurs, métriques) sont supprimées en cascade.

## 📊 Détails des Flow Meters (popup)

Le popup affiche pour chaque métrique :
- **Meter** (composant Talend)
- **Compteur** (label de la connexion)
- **Valeur** (`count`)
- **Référence** (nombre de lignes de référence)
- **Pourcentage** (`count / reference * 100`)
- **État** : pastille colorée correspondant au seuil atteint (min/moy/max) selon les couleurs définies dans `thresholds`.

## 🌐 Sources de données distantes (FTP, SFTP, SMB)

L’application peut lire les fichiers depuis des serveurs distants en utilisant les protocoles **FTP**, **SFTP** ou **SMB**.  
Pour cela, vous devez **ajouter les dépendances Maven** correspondantes et **modifier la classe `FileImportService`** afin d’utiliser un lecteur distant.

### 🔌 Protocoles supportés et exemples de configuration

#### 1. FTP / FTPS
- **Bibliothèque** : Apache Commons Net
- **Dépendance Maven** :
  ```xml
  <dependency>
      <groupId>commons-net</groupId>
      <artifactId>commons-net</artifactId>
      <version>3.9.0</version>
  </dependency>
  ```
- **Chemin dans l’admin** :  
  `ftp://user:password@host:port/chemin/vers/stats_file.txt`

#### 2. SFTP (SSH File Transfer Protocol)
- **Bibliothèque** : JSch (ou Apache MINA SSHD)
- **Dépendance Maven** :
  ```xml
  <dependency>
      <groupId>com.jcraft</groupId>
      <artifactId>jsch</artifactId>
      <version>0.1.55</version>
  </dependency>
  ```
- **Chemin dans l’admin** :  
  `sftp://user:password@host:port/chemin/vers/stats_file.txt`

#### 3. SMB / CIFS (partage Windows)
- **Bibliothèque** : SMBJ (recommandé) ou jcifs-ng
- **Dépendance Maven** :
  ```xml
  <dependency>
      <groupId>com.hierynomus</groupId>
      <artifactId>smbj</artifactId>
      <version>0.12.2</version>
  </dependency>
  ```
- **Chemin dans l’admin** :  
  `smb://DOMAIN;user:password@host/Partage/chemin/stats_file.txt`  
  > Le domaine est optionnel. Exemple sans domaine : `smb://user:password@192.168.1.50/Logs/stats_file.txt`

### ⚙️ Implémentation côté Java

Pour activer ces protocoles, vous devez modifier le service `FileImportService` afin qu’il délègue la lecture des fichiers à une classe utilitaire capable d’interpréter les préfixes (`ftp://`, `sftp://`, `smb://`).  
Une implémentation de référence est fournie dans la classe `RemoteFileReader` (à ajouter dans le package `service`). Une fois cette classe en place, remplacez les appels `new FileReader(path)` par `remoteFileReader.readFile(path)` dans les méthodes `importStats`, `importLogs` et `importMeters`.

### 🔒 Sécurité
- Ne stockez pas les mots de passe en clair dans `application.properties` ou en base de données. Utilisez des variables d’environnement ou un coffre‑fort.
- Pour SFTP, préférez l’authentification par clé privée.
- Pour SMB, utilisez `StrictHostKeyChecking=yes` en production.

## 📁 Structure du projet

```
talend-dashboard/
├── pom.xml
├── README.md
├── src/main/java/com/talend/talenddashboard/
│   ├── TalendDashboardApplication.java
│   ├── config/
│   │   ├── MachineInitializer.java
│   │   ├── MachineProperties.java
│   │   └── SchedulerConfig.java
│   ├── entity/
│   │   ├── Machine.java
│   │   ├── JobExecution.java
│   │   ├── ErrorLog.java
│   │   └── FlowMeter.java
│   ├── repository/
│   │   ├── MachineRepository.java
│   │   ├── JobExecutionRepository.java
│   │   ├── ErrorLogRepository.java
│   │   └── FlowMeterRepository.java
│   ├── service/
│   │   ├── FileImportService.java
│   │   └── DashboardService.java
│   ├── controller/
│   │   ├── DashboardController.java
│   │   ├── AdminController.java
│   │   └── rest/
│   │       ├── DashboardRestController.java
│   │       └── ExecutionController.java
│   └── scheduler/ImportScheduler.java
├── src/main/resources/
│   ├── application.properties
│   ├── static/
│   │   ├── css/style.css
│   │   └── js/dashboard.js
│   └── templates/
│       ├── fragments/header.html
│       ├── dashboard.html
│       └── admin.html
└── ...
```

## 🛠️ Technologies utilisées

- **Spring Boot 3.2.5**
- **Spring Data JPA** + Hibernate
- **SQLite** (base embarquée)
- **Thymeleaf** (templates serveur)
- **Bootstrap 5.3** (interface utilisateur)
- **Chart.js** (graphiques)

## 📝 Licence

Projet libre. Vous pouvez l’utiliser, le modifier et le distribuer selon vos besoins.
```