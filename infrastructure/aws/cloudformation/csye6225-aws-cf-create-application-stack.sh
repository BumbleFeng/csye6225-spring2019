set -e

echo "Enter NetWork Stack Name:"
read STACK_NAME

StackName=$STACK_NAME-csye6225-application

echo "KeyName:"
KeyName=$(aws ec2 describe-key-pairs|grep -m 1 KeyName|cut -d'"' -f4)
echo $KeyName

echo "ImageId:"
ImageId=$(aws ec2 describe-images --owners self|grep -m 1 ImageId|cut -d'"' -f4)
echo $ImageId

aws cloudformation create-stack --stack-name $StackName --template-body file://csye6225-cf-application.json --parameters ParameterKey=KeyName,ParameterValue=$KeyName ParameterKey=ImageId,ParameterValue=$ImageId 

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

