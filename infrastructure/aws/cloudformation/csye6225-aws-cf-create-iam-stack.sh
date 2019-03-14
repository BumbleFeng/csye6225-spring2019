set -e

echo "Enter Stack Name:"
read STACK_NAME
StackName=$STACK_NAME-csye6225-iam


echo "User List:"
aws iam list-users|grep UserName|cut -d'"' -f4
echo "Enter User Name For CircleCI:"
read UserName
#UserName="CircleCI"
#echo $UserName
echo "CreateDate:"
CreateDate=$(aws iam get-user --user-name $UserName|grep CreateDate|cut -d'"' -f4)
echo $CreateDate

echo "Bucket List:"
aws s3api list-buckets|grep \"Name\"|cut -d'"' -f4
echo "Enter Bucket Name You Want To Use:"
#read BucketName
BucketName="code-deploy.csye6225-spring2019-huangfe.me"
echo $BucketName
echo "CreationDate:"
CreationDate=$(aws s3api list-buckets|grep -A 1 $BucketName|cut -d'"' -f4)
echo $CreationDate|cut -d' ' -f2

echo "ApplicationName:"
#read ApplicationName
ApplicationName="csye6225-webapp"
echo $ApplicationName

aws cloudformation create-stack --stack-name $StackName --template-body file://csye6225-cf-iam.json --capabilities CAPABILITY_IAM --parameters \
ParameterKey=UserName,ParameterValue=$UserName ParameterKey=ApplicationName,ParameterValue=$ApplicationName ParameterKey=BucketName,ParameterValue=$BucketName

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

