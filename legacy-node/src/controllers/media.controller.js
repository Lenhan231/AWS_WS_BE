const AWS = require('aws-sdk');
const { v4: uuidv4 } = require('uuid');

// Configure AWS S3
const s3 = new AWS.S3({
  accessKeyId: process.env.AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
  region: process.env.AWS_S3_REGION
});

// Get presigned URL for upload
exports.getPresignedUrl = async (req, res) => {
  try {
    const { fileName, fileType } = req.body;

    if (!fileName || !fileType) {
      return res.status(400).json({
        status: 400,
        error: 'Bad Request',
        message: 'fileName and fileType are required',
        timestamp: new Date().toISOString()
      });
    }

    // Validate file type
    const allowedTypes = (process.env.ALLOWED_IMAGE_TYPES || 'image/jpeg,image/png,image/gif,image/webp').split(',');
    if (!allowedTypes.includes(fileType)) {
      return res.status(400).json({
        status: 400,
        error: 'Bad Request',
        message: 'Invalid file type. Allowed types: ' + allowedTypes.join(', '),
        timestamp: new Date().toISOString()
      });
    }

    // Generate unique file name
    const fileExtension = fileName.split('.').pop();
    const uniqueFileName = `${uuidv4()}.${fileExtension}`;
    const s3Key = `uploads/${req.user.id}/${uniqueFileName}`;

    // Generate presigned URL
    const presignedUrl = s3.getSignedUrl('putObject', {
      Bucket: process.env.AWS_S3_BUCKET_NAME,
      Key: s3Key,
      ContentType: fileType,
      Expires: 300 // URL expires in 5 minutes
    });

    // Generate public URL
    const publicUrl = `https://${process.env.AWS_S3_BUCKET_NAME}.s3.${process.env.AWS_S3_REGION}.amazonaws.com/${s3Key}`;

    res.json({
      presignedUrl,
      publicUrl,
      key: s3Key,
      expiresIn: 300
    });
  } catch (error) {
    console.error('Get presigned URL error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

// Confirm upload completion
exports.confirmUpload = async (req, res) => {
  try {
    const { key } = req.body;

    if (!key) {
      return res.status(400).json({
        status: 400,
        error: 'Bad Request',
        message: 'key is required',
        timestamp: new Date().toISOString()
      });
    }

    // Verify object exists in S3
    try {
      await s3.headObject({
        Bucket: process.env.AWS_S3_BUCKET_NAME,
        Key: key
      }).promise();

      res.json({
        success: true,
        message: 'Upload confirmed',
        url: `https://${process.env.AWS_S3_BUCKET_NAME}.s3.${process.env.AWS_S3_REGION}.amazonaws.com/${key}`
      });
    } catch (s3Error) {
      return res.status(404).json({
        status: 404,
        error: 'Not Found',
        message: 'File not found in S3',
        timestamp: new Date().toISOString()
      });
    }
  } catch (error) {
    console.error('Confirm upload error:', error);
    res.status(500).json({
      status: 500,
      error: 'Internal Server Error',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
};

