set -e

echo "Enter Stack Name:"
read STACK_NAME
StackName=$STACK_NAME-csye6225-application

echo "VPC List:"
aws ec2 describe-vpcs|awk '/Name/{getline; print}'|cut -d'"' -f4

echo "Enter VPC Name Prefix You Want To Use: "
read VPC_NAME
VpcName=$VPC_NAME-csye6225-vpc
echo "VpcId:"
VpcId=$(aws ec2 describe-vpcs --filter "Name=tag:Name,Values=$VpcName"|grep -m 1 VpcId|cut -d'"' -f4)
echo $VpcId
echo "SubnetId:"
SubnetId1=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" "Name=availability-zone,Values=us-east-1a"|grep SubnetId|cut -d'"' -f4)
echo $SubnetId1
SubnetId2=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" "Name=availability-zone,Values=us-east-1b"|grep SubnetId|cut -d'"' -f4)
echo $SubnetId2
SubnetId3=$(aws ec2 describe-subnets --filter "Name=vpc-id,Values=$VpcId" "Name=availability-zone,Values=us-east-1c"|grep SubnetId|cut -d'"' -f4)
echo $SubnetId3

echo "Key List:"
aws ec2 describe-key-pairs|grep KeyName|cut -d'"' -f4
echo "Enter KeyName You Want To Use: "
read KeyName
KeyFingerprint=$(aws ec2 describe-key-pairs --key-names $KeyName|grep KeyFingerprint|cut -d'"' -f4)
echo $KeyFingerprint

echo "AMI List:"
aws ec2 describe-images --owners self|grep \"Name\"|cut -d'"' -f4
echo "Enter AMI Name You Want To Use:"
read AMIName
echo "ImageId:"
ImageId=$(aws ec2 describe-images --owners self --filter "Name=name,Values=$AMIName"|grep ImageId|cut -d'"' -f4)
echo $ImageId

#echo "Bucket List:"
#aws s3api list-buckets|grep Name|cut -d'"' -f4
#echo "Enter Bucket Name You Want To Use:"
#read BucketName

echo "Bucket:"
BucketName="csye6225-spring2019-huangfe.me.csye6225.com"
echo $BucketName
echo "CreationDate:"
CreationDate=$(aws s3api list-buckets|grep -A 1 $BucketName|cut -d'"' -f4)
echo $CreationDate|cut -d' ' -f2

echo "DatabaseName:"
DatabaseName="csye6225"
echo $DatabaseName
echo "DatabaseUsername:"
DatabaseUsername="csye6225master"
echo $DatabaseUsername
echo "DatabasePassword:"
DatabasePassword="csye6225password"
echo $DatabasePassword

aws cloudformation create-stack --stack-name $StackName --template-body file://csye6225-cf-application.json --parameters \
ParameterKey=VPC,ParameterValue=$VpcId ParameterKey=Subnet1,ParameterValue=$SubnetId1 \
ParameterKey=Subnet2,ParameterValue=$SubnetId2 ParameterKey=Subnet3,ParameterValue=$SubnetId3 \
ParameterKey=KeyName,ParameterValue=$KeyName ParameterKey=ImageId,ParameterValue=$ImageId \
ParameterKey=BucketName,ParameterValue=$BucketName ParameterKey=DatabaseName,ParameterValue=$DatabaseName \
ParameterKey=DatabaseUsername,ParameterValue=$DatabaseUsername ParameterKey=DatabasePassword,ParameterValue=$DatabasePassword 


Status=$(aws cloudformation describe-stacks --stack-name $StackName|grep StackStatus|cut -d'"' -f4)

echo "Please wait..."

i=1
sp="/-\|"
echo -n ' '
while [ "$Status" != "CREATE_COMPLETE" ]
do
    if [ "$Status" == "ROLLBACK_COMPLETE" ]
    then
    	printf "\b"
        aws cloudformation describe-stacks --stack-name $StackName
        exit 1
    fi
    Status=$(aws cloudformation describe-stacks --stack-name  $StackName 2>&1|grep StackStatus|cut -d'"' -f4)
    printf "\b${sp:i++%${#sp}:1}"
done

printf "\b"
echo "CREATE_COMPLETE"

