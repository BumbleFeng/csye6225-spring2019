set -e

echo "Enter VPC Name:"
read VPC_NAME

VpcName=$VPC_NAME-csye6225-vpc

echo "Creating Vpc....."
VpcId=$(aws ec2 create-vpc --cidr-block 10.0.0.0/24 |grep VpcId |cut -d'"' -f4)
echo $VpcId

aws ec2 create-tags --resources $VpcId --tags Key=Name,Value=$VpcName


SubnetId1=$(aws ec2 create-subnet --vpc-id $VpcId --cidr-block 10.0.0.0/26 --availability-zone us-east-1a|grep SubnetId|cut -d'"' -f4)
echo $SubnetId1
SubnetId2=$(aws ec2 create-subnet --vpc-id $VpcId --cidr-block 10.0.0.64/26 --availability-zone us-east-1b|grep SubnetId|cut -d'"' -f4)
echo $SubnetId2
SubnetId3=$(aws ec2 create-subnet --vpc-id $VpcId --cidr-block 10.0.0.128/26 --availability-zone us-east-1c|grep SubnetId|cut -d'"' -f4)
echo $SubnetId3

echo "Creating InternetGateway..."
InternetGatewayId=$(aws ec2 create-internet-gateway|grep InternetGatewayId|cut -d'"' -f4)
echo $InternetGatewayId

echo "Attaching Gateway to VPC...."
aws ec2 attach-internet-gateway --internet-gateway-id $InternetGatewayId --vpc-id $VpcId

echo "Creating RouteTable........."
RouteTableId=$(aws ec2 create-route-table --vpc-id $VpcId|grep RouteTableId |cut -d'"' -f4)
echo $RouteTableId

echo "Adding subnet route to routeTable..........."
AssociationId1=$(aws ec2 associate-route-table --route-table-id $RouteTableId --subnet-id $SubnetId1|grep AssociationId|cut -d'"' -f4)
echo $AssociationId1
AssociationId2=$(aws ec2 associate-route-table --route-table-id $RouteTableId --subnet-id $SubnetId2|grep AssociationId|cut -d'"' -f4)
echo $AssociationId2
AssociationId3=$(aws ec2 associate-route-table --route-table-id $RouteTableId --subnet-id $SubnetId3|grep AssociationId|cut -d'"' -f4)
echo $AssociationId3

echo "Adding GatewayRoute to RouteTable............"
Result=$(aws ec2 create-route --route-table-id $RouteTableId --destination-cidr-block 0.0.0.0/0 --gateway-id $InternetGatewayId|grep true)
if [ -z "$Result" ];then
	echo "Failed to add gateway route to routeTable!"
	exit 1
fi
echo "Success"

echo "Modify the default security group ............"
GroupId=$(aws ec2 describe-security-groups --filter "Name=vpc-id,Values=$VpcId"|grep -m 1 GroupId|cut -d'"' -f4)
aws ec2 revoke-security-group-ingress --group-id $GroupId  --protocol all --source-group $GroupId
aws ec2 authorize-security-group-ingress --group-id $GroupId --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $GroupId --protocol tcp --port 22 --cidr 0.0.0.0/0

echo "CREATE_COMPLETE"


