# Detailed document for QA Infrastructure 

<p align="center">
 <img src="https://github.com/SheetalNain/salary/blob/main/assets/quality-assurance-1.png" alt="image" width="500" height="300" />
</p>

| **Author**            | **Created on** | **Version** | **Last updated by**       | **Last edited on** | **Reviewer** |**L0 Reviewer** |**L1 Reviewer** |**L2 Reviewer** |
|-----------------------|----------------|-------------|----------------------------|---------------------|-------------------|-------------------|-------------------|-------------------|
| Sheetal      | 31-03-25       | Version 1 | Sheetal         | 31-03-25     | Pritam   | Shikha  | Kirti  | Ashwani Singh  |
| Sheetal      | 31-03-25       | Version 2 | Sheetal         | 31-03-25     | Pritam    |   |   |   |

---

## **Table of Contents**

1. [Introduction](#introduction)
2. [Infrastructure Diagram](#Infrastructure-Diagram)
3. [Description](#description)
4. [Network Configuration](#Network-Configuration)
5. [Security and Access Control](#security-and-access-control)
6. [Load Balancing & Traffic Flow](#load-balancing--traffic-flow)
7. [Best Practices](#Best-Practices)
8. [Conclusion](#Conclusion)
9. [Contact Information](#Contact-Information)

---
## **Introduction**

The **OT-Microservices QA Infrastructure** is designed for scalability and security. The environment is deployed in **us-east-1 (North Virginia)** region, within a dedicated VPC named **"otms_vpc"**. 

---
## **Infrastructure Diagram**

<img src="https://github.com/SheetalNain/salary/blob/main/assets/QA.drawio.png" width="1000" height="1000">

---
## **Description**
| **Component**                           |**Details**                                      |
| ----------------------------------- | --------------------------------------------------------- |
| **User Access**                     | Users connect to the infrastructure through the internet.                           |
| **Route53**                         | DNS service for routing traffic to various components.                    |
| **Public Subnets**                  | Public subnets host components requiring direct internet access. Open VPN facilitates secure access.|
| **Private Subnets**                 | Frontend app, Attendance, Employee, and Salary APIs hosted in separate private subnets. Databases (PostgreSQL, Scylla, Redis) in another private subnet. |
| **Security Groups**                 |  To provide security on instance level |
| **NACLs (Network ACLs)**           | Control inbound and outbound traffic at subnet level for specific private subnets.                  |
| **Internet Gateway & NAT Gateway**  |  Internet Gateway (Igw) for VPC internet access.<br> NAT Gateway for outbound traffic from private subnets to the internet.|
| **Route Tables**                    |  public_rt for public subnet internet access.<br> private-rt for communication within the VPC.    |
| **ALB (Application Load Balancer)** | Configured to distribute frontend traffic across multiple targets for high availability.            |
| **Auto Scaling Group (ASG)**        | Dynamically adjusts frontend and API service instances.              |
| **VPC (Virtual Private Cloud)**     | Created for QA environments, organizes components for scalability and availability.        |


## **Network Configuration**

### **Virtual Private Cloud (VPC)**

|VPC Name|	CIDR Block|	Region|
|-------|----------|---------|
|otms_vpc	|192.168.64.0/22 |	us-east-1|

### **Availability Zones**

The infrastructure is deployed in a single availability zone.

| Availability Zone | Description                         |
| ----------------- | ----------------------------------- |
| us-east-1a        | Contains all the resources    |


---
### **Subnets**

Each subnet is designed for a specific purpose to ensure proper network segmentation and security.

| Subnet             | Availability Zone | CIDR Block      | Purpose                                  |
| ------------------ | ----------------- | --------------- | ---------------------------------------- |
| public_subnet      | us-east-1a        | 192.168.64.0/28    | OpenVPN Server                           |
| frontend_subnet    | us-east-1a        | 192.168.68.0/28   | Frontend Server and Load Balancer        |
| application_subnet | us-east-1a        | 192.168.72.0/28   | Salary, Employee, and Attendance Servers |
| database_subnet    | us-east-1a        | 192.168.80.0/28  | Redis, ScyllaDB, PostgreSQL              |


---
### **Route Tables**

Route tables control the traffic flow between subnets and external networks.

| Route Table         | Associated Subnets                          | Routes|
| ------------------- | ------------------------------------------- |------|
| public_rt  | public_subnet                               | 0.0.0.0/0 → otms_igw |
| private_rt | frontend_subnet, application_subnet, database_subnet  | 0.0.0.0/0 → public_NAT |

---
### **Gateways**

| Gateway                | Purpose                                                         |
| ---------------------- | --------------------------------------------------------------- |
| otms_igw | Enables internet access for public subnet                       |
| public_NAT            | Allows private subnet instances to access the internet securely |

---
### **DNS Management (Route 53)**

Amazon Route 53 is used for domain resolution and internal DNS routing.

| Service         | Purpose                                            |
| --------------- | -------------------------------------------------- |
| otms.site | Manages domain resolution and internal DNS routing |

---
## **Security and Access Control**

### **Security Groups**

#### **frontend_sg**

| Protocol | Port Range       | Source        |
| -------- | --------------- | ------------- |
| TCP      | 22            | openvpn_sg  |
| TCP      | 443            | openvpn_sg  |
| TCP      | 80         | alb_sg |
| TCP      | 3000         | alb_sg  |

---
#### **salary_sg**

| Protocol | Port Range       | Source           |
| -------- | --------------- | ---------------- |
| TCP      | 22            | openvpn_sg  |
| TCP      | 80         | alb_sg |
| TCP      | 8080            | alb_sg,frontend_sg & scylladb_sg   |


---
#### **employee_sg**

| Protocol | Port Range       | Source           |
| -------- | --------------- | ---------------- |
| TCP      | 22            | openvpn_sg  |
| TCP      | 80            | alb_sg |
| TCP      | 8080          | alb_sg , frontend_sg & scylladb_sg  |


---
#### **attendance_sg**

| Protocol | Port Range       | Source           |
| -------- | --------------- | ---------------- |
| TCP      | 22            | `openvpn_sg`  |
| TCP      | 80        | alb_sg |
| TCP      | 8080            | alb_sg, frontend_sg & postgresql_sg   |


---
#### **redis_sg**

| Protocol | Port Range       | Source        |
| -------- | --------------- | ------------- |
| TCP      | 22            | `openvpn_sg`  |
| TCP      | 6379            | salary_sg, employee_sg & attendance_sg |


---
#### **scylladb_sg**

| Protocol | Port Range       | Source        |
| -------- | --------------- | ------------- |
| TCP      | 22            | `openvpn_sg`  |
| TCP      | 9042            | salary_sg, employee_sg & redis_sg|


---
#### **postgresql_sg**

| Protocol | Port Range       | Source        |
| -------- | --------------- | ------------- |
| TCP      | 22            | `openvpn_sg`  |
| TCP      | 5432            | attendance_sg, redis_sg |

---

#### **alb_sg**

| **Protocol** | **Port Range**  | **Source**        | 
| -------- | ----------- | ------------- | 
| **TCP**  | 80  |  `openvpn_sg` | 
| **TCP**  | 443  |  `openvpn_sg` | 
| **TCP**  | 3000   | `openvpn_sg` | 
| **TCP**  | 8080  |  `openvpn_sg` | 

---

#### **openvpn_sg**

| Protocol | Port Range      | Source        |
| -------- | --------------- | ------------- |
| TCP      | 22              | 192.168.64.0/22  |
| TCP      | 80/443          |192.168.64.0/22 |

---
### **Network ACLs (NACLs)**

Network ACLs provide an additional layer of security at the subnet level.

---

#### **Frontend NACL Inbound Rules**

| Rule Number | Type           | Protocol  | Port Range | Source           | Allow/Deny |
|-------------|----------------|-----------|------------|------------------|------------|
| 90          | All traffic    | All       | All        | 0.0.0.0/0        | Allow      |
| 100         | SSH (22)       | TCP (6)   | 22         | 192.168.64.0/28  | Allow      |
| 120         | Custom TCP     | TCP (6)   | 3000       |192.168.64.0/28   | Allow      |
| 130         | HTTPS (443)    | TCP (6)   | 443        |192.168.64.0/22   | Allow      |
| 140         | HTTP (80)      | TCP (6)   | 80         |192.168.64.0/22   | Allow      |
| 180         | SSH (22)       | TCP (6)   | 22         | 192.168.72.0/28  | Allow      |
| *           | All traffic    | All       | All        | 0.0.0.0/0        | Deny       |

#### **Frontend NACL Outbound Rules**

| Rule Number | Type           | Protocol  | Port Range       | Destination       | Allow/Deny |
|-------------|----------------|-----------|------------------|-------------------|------------|
| 90          | All traffic    | All       | All              | 0.0.0.0/0         | Allow      |
| 100         | SSH (22)       | TCP (6)   | 22               |192.168.64.0/28    | Allow      |
| 120         | Custom TCP     | TCP (6)   | 1024 - 65535     | 0.0.0.0/0         | Allow      |
| 160         | HTTP (80)      | TCP (6)   | 80               | 0.0.0.0/0         | Allow      |
| 180         | HTTPS (443)    | TCP (6)   | 443              | 0.0.0.0/0         | Allow      |
| 200         | Custom TCP     | TCP (6)   | 8080             |192.168.72.0/28    | Allow      |
| *           | All traffic    | All       | All              | 0.0.0.0/0         | Deny       |

---

#### **application_nacl Inbound Rules**

| Rule number | Type      | Protocol | Port range | Source       | Allow/Deny |
|-------------|-----------|----------|------------|--------------|------------|
| 100         | SSH       | TCP      | 22         | 192.168.64.0/28  | Allow     |
| 110         | SSH       | TCP      | 22         | 192.168.68.0/28  | Allow    |
|110          | Custom TCP |TCP (6)  | 1024 - 65535 | 0.0.0.0/0      | Allow       |
| 110         | Custom TCP| TCP      | 8080         |192.168.64.0/28 | Allow     |
| 120         | Http        | TCP    | 80         | 192.168.64.0/22  | Allow      |
| 120         | Http        | TCP    | 443        |192.168.64.0/22   | Allow       |
| 130         | Http        | TCP    | 80         | 192.168.64.0/28  | Allow     |
| *           | All traffic | All    | All        | 0.0.0.0/0        | Deny       |

#### **application_nacl Outbound Rules**

| Rule Number | Type           | Protocol  | Port Range       | Destination       | Allow/Deny |
|-------------|----------------|-----------|------------------|-------------------|------------|
| 80          | Custom TCP     | TCP (6)   | 1024 - 65535     | 0.0.0.0/0         | Allow      |
| 120         | HTTPS (443)    | TCP (6)   | 443              | 0.0.0.0/0         | Allow      |
| 130         | HTTP (80)      | TCP (6)   | 80               | 0.0.0.0/0         | Allow      |
| 140         | Custom TCP     | TCP (6)   | 8080             | 192.168.68.0/28   | Allow      |
| 150         | Custom TCP     | TCP (6)   | 8080             | 192.168.64.0/28   | Allow      |
| 200         | Custom TCP     | TCP (6)   | 9042             | 192.168.68.0/28   | Allow      |
| 300         | Custom TCP     | TCP (6)   | 6379             | 192.168.68.0/28   | Allow      |
| 400         | PostgreSQL (5432) | TCP (6) | 5432            | 192.168.68.0/28   | Allow      |
| *           | All traffic    | All       | All              | 0.0.0.0/0         | Deny       |

---

#### **Database NACL Inbound Rules**

| Rule Number | Type           | Protocol  | Port Range       | Source             | Allow/Deny |
|-------------|----------------|-----------|------------------|--------------------|------------|
| 80          | All traffic    | All       | All              | 0.0.0.0/0          | Allow      |
| 90          | Custom TCP     | TCP (6)   | 1024 - 65535     | 0.0.0.0/0          | Allow      |
| 100         | SSH (22)       | TCP (6)   | 22               | 192.168.64.0/28    | Allow      |
| 200         | Custom TCP     | TCP (6)   | 9042             | 192.168.72.0/28    | Allow      |
| 300         | Custom TCP     | TCP (6)   | 6379             | 192.168.72.0/28    | Allow      |
| 400         | PostgreSQL (5432) | TCP (6) | 5432            | 192.168.72.0/28    | Allow      |
| *           | All traffic    | All       | All              | 0.0.0.0/0          | Deny       |

#### **Database NACL Outbound Rules**

| Rule Number | Type           | Protocol  | Port Range       | Destination        | Allow/Deny |
|-------------|----------------|-----------|------------------|--------------------|------------|
| 80          | All traffic    | All       | All              | 0.0.0.0/0          | Allow      |
| 90          | Custom TCP     | TCP (6)   | 1024 - 65535     | 0.0.0.0/0          | Allow      |
| 100         | SSH (22)       | TCP (6)   | 22               | 192.168.64.0/28    | Allow      |
| 200         | Custom TCP     | TCP (6)   | 1024 - 65535     | 192.168.72.0/28    | Allow      |
| 300         | Custom TCP     | TCP (6)   | 8080             | 192.168.72.0/28    | Allow      |
| *           | All traffic    | All       | All              | 0.0.0.0/0          | Deny       |

---
## **Load Balancing & Traffic Flow**

Load balancing ensures high availability and efficient traffic distribution across microservices.

| Load Balancer          | Routing Type       | Target Servers                                 |
| ---------------------- | ------------------ | ---------------------------------------------- |
| otms_alb | Path-based Routing | frontend, salary, employee, attendance Servers |

---
### **Path-Based Routing**

| Service        | Path                  |
| -------------- | --------------------- |
| Salary API     | /salary-documentation |
| Employee API   | /swagger/index.html   |
| Attendance API | /apidocs              |

---

### **Best Practices**  

| Area                 | Best Practice |
|----------------------|--------------------------------------------------------------|
| **Security**        | Restrict access using **least privilege** IAM roles and **IP whitelisting**. |
| **Networking**      | Use **private subnets** for databases and backend services. |
| **Load Balancing**  | Implement **ALB** with path-based routing for efficient traffic distribution. |
| **Infrastructure**  | Use **Terraform & Ansible** for consistent infrastructure management. |
| **Monitoring**      | Enable **CloudWatch logs** and plan for **Elasticsearch + Kibana** for logging. |
| **Scalability**     | Configure **auto-scaling** for handling traffic spikes. |
| **Database**        | Deploy **PostgreSQL & ScyllaDB in multi-AZ** for high availability. |
| **Cost Optimization** | Use **reserved instances** and optimize compute resources. |
| **Backup & Recovery** | Automate **database backups** and enable **point-in-time recovery**. |


---
## **Conclusion**

The OT-Microservices QA infrastructure is designed to provide a secure and scalable environment for microservices-based applications. By implementing structured network configurations, security policies, and load balancing mechanisms, this QA environment ensures test stability, controlled traffic management, and secure isolation from production. 

## **Contact Information**
| Name | Email | 
|------|--------------------------|
| Sheetal | sheetal.nain@mygurukulam.co | 
