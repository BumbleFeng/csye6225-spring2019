set -e

echo "Enter VPC You Want To Delete: "
read VPC_NAME

VpcName=$VPC_NAME-csye6225-vpc

echo "VpcId:"
VpcId=$(aws ec2 describe-vpcs --filter "Name=tag:Name,Values=$VpcName"|grep -m 1 VpcId|cut -d'"' -f4)
echo $VpcId

echo "SubnetId:"
SubnetId1=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" --query 'Subnets[0]'|grep SubnetId|cut -d'"' -f4)
echo $SubnetId1
SubnetId2=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" --query 'Subnets[1]'|grep SubnetId|cut -d'"' -f4)
echo $SubnetId2
SubnetId3=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" --query 'Subnets[2]'|grep SubnetId|cut -d'"' -f4)
echo $SubnetId3
SubnetId4=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" --query 'Subnets[3]'|grep SubnetId|cut -d'"' -f4)
echo $SubnetId4
SubnetId5=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" --query 'Subnets[4]'|grep SubnetId|cut -d'"' -f4)
echo $SubnetId5
SubnetId6=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" --query 'Subnets[5]'|grep SubnetId|cut -d'"' -f4)
echo $SubnetId6


echo "InternetGatewayId:"
InternetGatewayId=$(aws ec2 describe-internet-gateways --filter "Name=attachment.vpc-id,Values=$VpcId"|grep InternetGatewayId|cut -d'"' -f4)
echo $InternetGatewayId

echo "RouteTableId:"
RouteTableId=$(aws ec2 describe-route-tables --filter "Name=route.gateway-id,Values=$InternetGatewayId"|grep -m 1 RouteTableId|cut -d'"' -f4)
echo $RouteTableId

echo "AssociationId:"
AssociationId1=$(aws ec2 describe-route-tables --filter "Name=route.gateway-id,Values=$InternetGatewayId" --query 'RouteTables[0].Associations[0]'|grep RouteTableAssociationId|cut -d'"' -f4)
echo $AssociationId1
AssociationId2=$(aws ec2 describe-route-tables --filter "Name=route.gateway-id,Values=$InternetGatewayId" --query 'RouteTables[0].Associations[1]'|grep RouteTableAssociationId|cut -d'"' -f4)
echo $AssociationId2
AssociationId3=$(aws ec2 describe-route-tables --filter "Name=route.gateway-id,Values=$InternetGatewayId" --query 'RouteTables[0].Associations[2]'|grep RouteTableAssociationId|cut -d'"' -f4)
echo $AssociationId3
AssociationId4=$(aws ec2 describe-route-tables --filter "Name=route.gateway-id,Values=$InternetGatewayId" --query 'RouteTables[0].Associations[3]'|grep RouteTableAssociationId|cut -d'"' -f4)
echo $AssociationId4
AssociationId5=$(aws ec2 describe-route-tables --filter "Name=route.gateway-id,Values=$InternetGatewayId" --query 'RouteTables[0].Associations[4]'|grep RouteTableAssociationId|cut -d'"' -f4)
echo $AssociationId5
AssociationId6=$(aws ec2 describe-route-tables --filter "Name=route.gateway-id,Values=$InternetGatewayId" --query 'RouteTables[0].Associations[5]'|grep RouteTableAssociationId|cut -d'"' -f4)
echo $AssociationId6

echo "Deleting gateway route from routeTable........."
aws ec2 delete-route --route-table-id $RouteTableId --destination-cidr-block 0.0.0.0/0

echo "Disassociating subnets from routeTable........."
aws ec2 disassociate-route-table --association-id $AssociationId1
aws ec2 disassociate-route-table --association-id $AssociationId2
aws ec2 disassociate-route-table --association-id $AssociationId3
aws ec2 disassociate-route-table --association-id $AssociationId4
aws ec2 disassociate-route-table --association-id $AssociationId5
aws ec2 disassociate-route-table --association-id $AssociationId6

echo "Deleting routeTable............................"
aws ec2 delete-route-table --route-table-id $RouteTableId

echo "Detaching internetGateway from VPC............."
aws ec2 detach-internet-gateway --internet-gateway-id $InternetGatewayId --vpc-id $VpcId

echo "DDeleting internetGateway......................."
aws ec2 delete-internet-gateway --internet-gateway-id $InternetGatewayId

echo "Deleting subnets..............................."
aws ec2 delete-subnet --subnet-id $SubnetId1
aws ec2 delete-subnet --subnet-id $SubnetId2
aws ec2 delete-subnet --subnet-id $SubnetId3
aws ec2 delete-subnet --subnet-id $SubnetId4
aws ec2 delete-subnet --subnet-id $SubnetId5
aws ec2 delete-subnet --subnet-id $SubnetId6

echo "Deleting VPC..................................."
aws ec2 delete-vpc --vpc-id $VpcId 

echo "DELETE_COMPLETE"
