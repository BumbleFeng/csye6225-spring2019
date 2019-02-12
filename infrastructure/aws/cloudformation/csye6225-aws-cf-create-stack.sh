echo "Enter NetWork Stack Name:"
read STACK_NAME

StackName=$STACK_NAME-csye6225-vpc
RouteTableName=$STACK_NAME-csye6225-rt

aws cloudformation create-stack --stack-name $StackName --template-body file://csye6225-cf-networking.json --parameters ParameterKey=RouteTableName,ParameterValue=$RouteTableName

Status=$(aws cloudformation describe-stacks --stack-name $StackName | grep StackStatus| cut -d'"' -f4)


echo "Please wait..."

i=1
sp="/-\|"
echo -n ' '
while [ "$Status" != "CREATE_COMPLETE" ]
do
    if [ "$Status" == "CREATE_FAILED" ]
    then
        aws cloudformation describe-stacks --stack-name $StackName
        exit 1
    fi
    Status=$(aws cloudformation describe-stacks --stack-name  $StackName 2>&1 | grep StackStatus| cut -d'"' -f4)
    printf "\b${sp:i++%${#sp}:1}"
done

printf "\b"
echo "CREATE_COMPLETE"

