## Azure

# 1. Introdução
O Microsoft Azure agora oferece suporte a Java bastante sólido.

Neste tutorial, vamos demonstrar como fazer nosso aplicativo Spring Boot funcionar na plataforma Azure, passo a passo.

# 2. Dependência e configuração do Maven
Primeiro, precisamos de uma assinatura do Azure para usar os serviços de nuvem lá; atualmente, podemos inscrever uma conta gratuita aqui.

Em seguida, faça login na plataforma e crie uma entidade de serviço usando a CLI do Azure:

```
> az login
To sign in, use a web browser to open the page \
https://microsoft.com/devicelogin and enter the code XXXXXXXX to authenticate.
> az ad sp create-for-rbac --name "app-name" --password "password"
{
    "appId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "displayName": "app-name",
    "name": "http://app-name",
    "password": "password",
    "tenant": "tttttttt-tttt-tttt-tttt-tttttttttttt"
}
```

Agora, definimos as configurações de autenticação principal de serviço do Azure em nosso Maven settings.xml, com a ajuda da seção a seguir, em <<servers>>:

```
<server>
    <id>azure-auth</id>
    <configuration>
        <client>aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa</client>
        <tenant>tttttttt-tttt-tttt-tttt-tttttttttttt</tenant>
        <key>password</key>
        <environment>AZURE</environment>
    </configuration>
</server>
```

Contaremos com a configuração de autenticação acima ao enviar nosso aplicativo Spring Boot para a plataforma Microsoft, usando azure-webapp-maven-plugin.

Vamos adicionar o seguinte plugin Maven ao pom.xml:

```
<plugin>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-webapp-maven-plugin</artifactId>
    <version>1.1.0</version>
    <configuration>
        <!-- ... -->
    </configuration>
</plugin>
```

Podemos verificar a última versão de lançamento aqui.

Existem várias propriedades configuráveis para este plug-in que serão abordadas na introdução a seguir.

# 3. Implantar um aplicativo Spring Boot no Azure
Agora que configuramos o ambiente, vamos tentar implantar nosso aplicativo Spring Boot no Azure.

Nosso aplicativo responde com “olá azul!” quando acessamos "/hello":

```
@GetMapping("/hello")
public String hello() {
    return "hello azure!";
}
```

A plataforma agora permite a implantação de Java Web App para Tomcat e Jetty. Com o azure-webapp-maven-plugin, podemos implantar nosso aplicativo diretamente em contêineres da web com suporte como o aplicativo padrão (ROOT) ou implantar via FTP.

Observe que, como vamos implementar o aplicativo em contêineres da web, devemos empacotá-lo como um arquivo WAR. Como um lembrete rápido, temos um artigo que apresenta como implantar um Spring Boot WAR no Tomcat.

### 3.1. Implantação de contêiner da web
Usaremos a seguinte configuração para azure-webapp-maven-plugin se pretendemos implantar no Tomcat em uma instância do Windows:

```
<configuration>
    <javaVersion>1.8</javaVersion>
    <javaWebContainer>tomcat 8.5</javaWebContainer>
    <!-- ... -->
</configuration>
```

Para uma instância do Linux, tente a seguinte configuração:

```
<configuration>
    <linuxRuntime>tomcat 8.5-jre8</linuxRuntime>
    <!-- ... -->
</configuration>
```

Não vamos esquecer a autenticação do Azure:

```
<configuration>
    <authentication>
        <serverId>azure-auth</serverId>
    </authentication>
    <appName>spring-azure</appName>
    <resourceGroup>baeldung</resourceGroup>
    <!-- ... -->
</configuration>
```

Quando implantamos nosso aplicativo no Azure, vamos vê-lo aparecer como um Serviço de Aplicativo. Portanto, aqui especificamos a propriedade <<appName>> para nomear o Serviço de Aplicativo. Além disso, o Serviço de Aplicativo, como um recurso, precisa ser mantido por um contêiner de grupo de recursos, portanto, <<resourceGroup>> também é necessário.

Agora estamos prontos para puxar o gatilho usando o azure-webapp: implantar o destino Maven e veremos a saída:


```
> mvn clean package azure-webapp:deploy
...
[INFO] Start deploying to Web App spring-isaccanedo...
[INFO] Authenticate with ServerId: azure-auth
[INFO] [Correlation ID: cccccccc-cccc-cccc-cccc-cccccccccccc] \
Instance discovery was successful
[INFO] Target Web App doesn't exist. Creating a new one...
[INFO] Creating App Service Plan 'ServicePlanssssssss-bbbb-0000'...
[INFO] Successfully created App Service Plan.
[INFO] Successfully created Web App.
[INFO] Starting to deploy the war file...
[INFO] Successfully deployed Web App at \
https://spring-isaccanedo.azurewebsites.net
...
```

Agora podemos acessar https://spring-isaccanedo.azurewebsites.net/hello e ver a resposta: 'hello azure!'

Durante o processo de implantação, o Azure criou automaticamente um Plano de Serviço de Aplicativo para nós. Confira o documento oficial para obter detalhes sobre os planos do Serviço de Aplicativo do Azure. Se já temos um plano de serviço de aplicativo, podemos definir a propriedade <<appServicePlanName>> para evitar a criação de um novo:

```
<configuration>
    <!-- ... -->
    <appServicePlanName>ServicePlanssssssss-bbbb-0000</appServicePlanName>
</configuration>
```

### 3.2. Implementação FTP
Para implantar via FTP, podemos usar a configuração:

```
<configuration>
    <authentication>
        <serverId>azure-auth</serverId>
    </authentication>
    <appName>spring-baeldung</appName>
    <resourceGroup>baeldung</resourceGroup>
    <javaVersion>1.8</javaVersion>

    <deploymentType>ftp</deploymentType>
    <resources>
        <resource>
            <directory>${project.basedir}/target</directory>
            <targetPath>webapps</targetPath>
            <includes>
                <include>*.war</include>
            </includes>
        </resource>
    </resources>
</configuration>
```
    
Na configuração acima, fazemos com que o plug-in localize o arquivo WAR no diretório $ {project.basedir} 
/target e o implemente no diretório webapps do contêiner Tomcat.

Digamos que nosso artefato final seja denominado azure-0.1.war, veremos uma saída como a seguinte assim que iniciarmos a implantação:    
    
```
> mvn clean package azure-webapp:deploy
...
[INFO] Start deploying to Web App spring-isaccanedo...
[INFO] Authenticate with ServerId: azure-auth
[INFO] [Correlation ID: cccccccc-cccc-cccc-cccc-cccccccccccc] \
Instance discovery was successful
[INFO] Target Web App doesn't exist. Creating a new one...
[INFO] Creating App Service Plan 'ServicePlanxxxxxxxx-xxxx-xxxx'...
[INFO] Successfully created App Service Plan.
[INFO] Successfully created Web App.
...
[INFO] Finished uploading directory: \
/xxx/.../target/azure-webapps/spring-isaccanedo --> /site/wwwroot
[INFO] Successfully uploaded files to FTP server: \
xxxx-xxxx-xxx-xxx.ftp.azurewebsites.windows.net
[INFO] Successfully deployed Web App at \
https://spring-isaccanedo.azurewebsites.net 
```
    
Observe que aqui não implantamos nosso aplicativo como o aplicativo da Web padrão para Tomcat, portanto, só podemos acessá-lo por meio de ‘https://spring-baeldung.azurewebsites.net/azure-0.1/hello '. O servidor irá responder 'hello azure!' como esperado.

# 4. Implantar com configurações de aplicativo personalizadas
Na maioria das vezes, nosso aplicativo Spring Boot requer acesso a dados para fornecer serviços. O Azure agora oferece suporte a bancos de dados como SQL Server, MySQL e PostgreSQL.

Para fins de simplicidade, usaremos seu MySQL no aplicativo como nossa fonte de dados, pois sua configuração é bastante semelhante a outros serviços de banco de dados do Azure.

### 4.1. Habilitar MySQL no aplicativo no Azure
Como não há uma linha única para criar um aplicativo da web com MySQL no aplicativo habilitado, temos que primeiro criar o aplicativo da web usando a CLI:
    
```
az group create --location japanwest --name isaccanedo-group
az appservice plan create --name isaccanedo-plan --resource-group isaccanedo-group --sku B1
az webapp create --name isaccanedo-webapp --resource-group isaccanedo-group \
  --plan isaccanedo-plan --runtime java|1.8|Tomcat|8.5
```
Em seguida, habilite o MySQL no aplicativo no portal:

<img src="azure.jpg">   
